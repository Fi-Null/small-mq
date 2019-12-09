package com.small.mq.admin.server;

import com.small.mq.admin.dao.MessageDao;
import com.small.mq.admin.task.*;
import com.small.mq.client.broker.Broker;
import com.small.mq.client.message.Message;
import com.small.mq.client.message.MessageStatus;
import com.small.mq.client.util.IpUtil;
import com.small.mq.client.util.ThreadPoolUtil;
import com.small.rpc.registry.smallregistry.SmallRegistryServiceRegistry;
import com.small.rpc.remoting.net.netty.server.NettyServer;
import com.small.rpc.remoting.provider.RpcProviderFactory;
import com.small.rpc.serialize.hessian.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/6/19 4:22 PM
 */
@Component
public class BrokerServer implements Broker, InitializingBean, DisposableBean {

    private final static Logger logger = LoggerFactory.getLogger(BrokerServer.class);


    // ---------------------- param ----------------------

    @Value("${small-mq.rpc.remoting.ip}")
    private String ip;

    @Value("${small-mq.rpc.remoting.port}")
    private int port;

    @Value("${small.mq.log.logretentiondays}")
    private int logretentiondays;

    @Value("${small.registry.address}")
    private String registryAddress;

    @Value("${small.registry.accessToken}")
    private String accessToken;

    @Resource
    private MessageDao messageDao;

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailUserName;

    @Override
    public void afterPropertiesSet() throws Exception {
        // init server
        initServer();

        // init thread
        initThead();
    }

    @Override
    public void destroy() throws Exception {
        // destory server
        destoryServer();

        // destory thread
        destroyThread();
    }

    // ---------------------- broker thread ----------------------
    private LinkedBlockingQueue<Message> newMessageQueue = new LinkedBlockingQueue<>();

    private LinkedBlockingQueue<Message> callbackMessageQueue = new LinkedBlockingQueue<>();

    private Map<String, Long> alarmMessageInfo = new ConcurrentHashMap<>();

    private ExecutorService executorService = ThreadPoolUtil.makeServerThreadPool("small-mq brokerserver", 3, 10);

    private volatile boolean executorStoped = false;

    public void initThead() throws Exception {

        /**
         * async save message, mult thread  (by event)
         */
        for (int i = 0; i < 3; i++) {
            executorService.execute(new SaveMessageTask(this));
        }

        /**
         * async callback message, mult thread  (by event)
         */
        for (int i = 0; i < 3; i++) {
            executorService.execute(new UpdateMessageTask(this));
        }

        executorService.execute(new ResetBlockTimeOutMessageTask(this));

        /**
         * auto alarm "check topic fail count, send alarm"  (by cycle, 1/60s)
         */
        executorService.execute(new AlarmTask(this));
        /**
         * auto clean success message  (by cycle, 1/>=3day)
         */
        if (logretentiondays >= 3) {
            executorService.execute(new CleanSuccessMessageTask(this));
        }
    }

    public void destroyThread() {
        executorStoped = true;
        executorService.shutdownNow();
    }

    // ---------------------- broker server ----------------------

    private RpcProviderFactory providerFactory;

    public void initServer() throws Exception {
        // address
        ip = (ip != null && ip.trim().length() > 0) ? ip : IpUtil.getIp();

        providerFactory = new RpcProviderFactory();
        providerFactory.setServer(NettyServer.class);
        providerFactory.setSerializer(HessianSerializer.class);
        providerFactory.setCorePoolSize(-1);
        providerFactory.setMaxPoolSize(-1);
        providerFactory.setIp(ip);
        providerFactory.setPort(port);
        providerFactory.setAccessToken(null);
        providerFactory.setServiceRegistry(SmallRegistryServiceRegistry.class);
        providerFactory.setServiceRegistryParam(new HashMap<String, String>() {{
            put(SmallRegistryServiceRegistry.SMALL_REGISTRY_ADDRESS, registryAddress);
            put(SmallRegistryServiceRegistry.ACCESS_TOKEN, accessToken);
        }});

        // add services
        providerFactory.addService(Broker.class.getName(), null, this);

        // start
        providerFactory.start();
    }

    public void destoryServer() throws Exception {
        // stop server
        if (providerFactory != null) {
            providerFactory.stop();
        }
    }

    // ---------------------- broker api ----------------------
    @Override
    public int addMessages(List<Message> messages) {
        newMessageQueue.addAll(messages);
        return messages.size();
    }

    @Override
    public List<Message> pullNewMessage(String topic, String group, int consumerRank, int consumerTotal, int pagesize) {
        List<Message> list = messageDao.pullNewMessage(MessageStatus.NEW.name(), topic, group, consumerRank, consumerTotal, pagesize);
        return list;
    }

    @Override
    public int lockMessage(long id, String appendLog) {
        return messageDao.lockMessage(id, appendLog, MessageStatus.NEW.name(), MessageStatus.RUNNING.name());
    }

    @Override
    public int callbackMessages(List<Message> messages) {
        callbackMessageQueue.addAll(messages);
        return messages.size();
    }

    public LinkedBlockingQueue<Message> getNewMessageQueue() {
        return newMessageQueue;
    }

    public void setNewMessageQueue(LinkedBlockingQueue<Message> newMessageQueue) {
        this.newMessageQueue = newMessageQueue;
    }

    public LinkedBlockingQueue<Message> getCallbackMessageQueue() {
        return callbackMessageQueue;
    }

    public void setCallbackMessageQueue(LinkedBlockingQueue<Message> callbackMessageQueue) {
        this.callbackMessageQueue = callbackMessageQueue;
    }

    public boolean isExecutorStoped() {
        return executorStoped;
    }

    public void setExecutorStoped(boolean executorStoped) {
        this.executorStoped = executorStoped;
    }

    public Map<String, Long> getAlarmMessageInfo() {
        return alarmMessageInfo;
    }

    public void setAlarmMessageInfo(Map<String, Long> alarmMessageInfo) {
        this.alarmMessageInfo = alarmMessageInfo;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String getEmailUserName() {
        return emailUserName;
    }

    public void setEmailUserName(String emailUserName) {
        this.emailUserName = emailUserName;
    }

    public int getLogretentiondays() {
        return logretentiondays;
    }

    public void setLogretentiondays(int logretentiondays) {
        this.logretentiondays = logretentiondays;
    }
}
