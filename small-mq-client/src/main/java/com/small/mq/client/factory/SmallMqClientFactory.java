package com.small.mq.client.factory;

import com.small.mq.client.broker.Broker;
import com.small.mq.client.consumer.ConsumerThread;
import com.small.mq.client.consumer.MessageListener;
import com.small.mq.client.consumer.annotation.Consumer;
import com.small.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.small.mq.client.message.Message;
import com.small.mq.client.util.ThreadPoolUtil;
import com.small.rpc.registry.smallregistry.SmallRegistryServiceRegistry;
import com.small.rpc.remoting.invoker.RpcInvokerFactory;
import com.small.rpc.remoting.invoker.call.CallType;
import com.small.rpc.remoting.invoker.reference.RpcReferenceBean;
import com.small.rpc.remoting.invoker.route.LoadBalance;
import com.small.rpc.remoting.net.netty.client.NettyClient;
import com.small.rpc.serialize.hessian.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 3:22 PM
 */
public class SmallMqClientFactory {

    private final static Logger logger = LoggerFactory.getLogger(SmallMqClientFactory.class);

    // ---------------------- param  ----------------------
    private String adminAddress;
    private String accessToken;
    private List<MessageListener> consumerList;

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setConsumerList(List<MessageListener> consumerList) {
        this.consumerList = consumerList;
    }

    // ---------------------- init destroy  ----------------------

    private void validConsumer() {
        // valid
        if (consumerList == null || consumerList.size() == 0) {
            logger.warn(">>>>>>>>>>> small-mq, MqConsumer not found.");
            return;
        }

        // make ConsumerThread
        for (MessageListener consumer : consumerList) {

            // valid annotation
            Consumer annotation = consumer.getClass().getAnnotation(Consumer.class);
            if (annotation == null) {
                throw new RuntimeException("small-mq, MqConsumer(" + consumer.getClass() + "), annotation is not exists.");
            }

            // valid group
            if (annotation.group() == null || annotation.group().trim().length() == 0) {
                // empty group means consume broadcase message, will replace by uuid
                try {
                    // annotation memberValues
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
                    Field mValField = invocationHandler.getClass().getDeclaredField("memberValues");
                    mValField.setAccessible(true);
                    Map memberValues = (Map) mValField.get(invocationHandler);

                    // set data for "group"
                    String randomGroup = UUID.randomUUID().toString().replaceAll("-", "");
                    memberValues.put("group", randomGroup);
                } catch (Exception e) {
                    throw new RuntimeException("small-mq, MqConsumer(" + consumer.getClass() + "), group empty and genereta error.");
                }

            }
            if (annotation.group() == null || annotation.group().trim().length() == 0) {
                throw new RuntimeException("small-mq, MqConsumer(" + consumer.getClass() + "),group is empty.");
            }

            // valid topic
            if (annotation.topic() == null || annotation.topic().trim().length() == 0) {
                throw new RuntimeException("small-mq, MqConsumer(" + consumer.getClass() + "), topic is empty.");
            }

            // consumer map
            consumerRespository.add(new ConsumerThread(consumer));
        }
    }


    public void init() throws Exception {

        // pre : valid consumer
        validConsumer();


        // start BrokerService
        startBrokerService();

        // start consumer
        startConsumer();

    }

    public void destroy() throws Exception {

        // pre : destory ClientFactoryThreadPool
        destoryClientFactoryThreadPool();


        // destory Consumer
        destoryConsumer();

        // destory BrokerService
        destoryBrokerService();

    }

    // ---------------------- broker service ----------------------
    private RpcInvokerFactory rpcInvokerFactory = null;
    private static Broker broker;
    private static ConsumerRegistryHelper consumerRegistryHelper = null;
    private static LinkedBlockingQueue<Message> newMessageQueue = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<Message> callbackMessageQueue = new LinkedBlockingQueue<>();

    public static ConsumerRegistryHelper getConsumerRegistryHelper() {
        return consumerRegistryHelper;
    }

    public static Broker getBroker() {
        return broker;
    }

    public static void addMessages(Message mqMessage, boolean async) {
        if (async) {
            // async queue, mult send
            newMessageQueue.add(mqMessage);
        } else {
            // sync rpc, one send
            broker.addMessages(Arrays.asList(mqMessage));
        }
    }

    public static void callbackMessage(Message mqMessage) {
        callbackMessageQueue.add(mqMessage);
    }

    public void startBrokerService() throws Exception {
        // init SmallRpcInvokerFactory
        rpcInvokerFactory = new RpcInvokerFactory(SmallRegistryServiceRegistry.class, new HashMap<String, String>() {{
            put(SmallRegistryServiceRegistry.SMALL_REGISTRY_ADDRESS, adminAddress);
            put(SmallRegistryServiceRegistry.ACCESS_TOKEN, accessToken);
        }});

        rpcInvokerFactory.start();

        // init ConsumerRegistryHelper
        SmallRegistryServiceRegistry commonServiceRegistry = (SmallRegistryServiceRegistry) rpcInvokerFactory.getServiceRegistry();
        consumerRegistryHelper = new ConsumerRegistryHelper(commonServiceRegistry);

        // init broker
        RpcReferenceBean referenceBean = new RpcReferenceBean();
        referenceBean.setClient(NettyClient.class);
        referenceBean.setSerializer(HessianSerializer.class);
        referenceBean.setCallType(CallType.SYNC);
        referenceBean.setLoadBalance(LoadBalance.ROUND);
        referenceBean.setIface(Broker.class);
        referenceBean.setVersion(null);
        referenceBean.setTimeout(5000);
        referenceBean.setAddress(null);
        referenceBean.setAccessToken(null);
        referenceBean.setInvokeCallback(null);
        referenceBean.setInvokerFactory(rpcInvokerFactory);

        broker = (Broker) referenceBean.getObject();

        // async + mult, addMessages
        for (int i = 0; i < 3; i++) {
            clientFactoryThreadPool.execute(() -> {
                while (!SmallMqClientFactory.clientFactoryPoolStoped) {
                    try {
                        Message message = newMessageQueue.take();
                        if (message != null) {
                            // load
                            List<Message> messageList = new ArrayList<>();
                            messageList.add(message);

                            List<Message> otherMessageList = new ArrayList<>();
                            int drainToNum = newMessageQueue.drainTo(otherMessageList, 100);
                            if (drainToNum > 0) {
                                messageList.addAll(otherMessageList);
                            }

                            // save
                            broker.addMessages(messageList);
                        }
                    } catch (Exception e) {
                        if (!SmallMqClientFactory.clientFactoryPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

                // finally total
                List<Message> otherMessageList = new ArrayList<>();
                int drainToNum = newMessageQueue.drainTo(otherMessageList);
                if (drainToNum > 0) {
                    broker.addMessages(otherMessageList);
                }
            });
        }

        // async + mult, addMessages
        for (int i = 0; i < 3; i++) {
            clientFactoryThreadPool.execute(() -> {
                while (!SmallMqClientFactory.clientFactoryPoolStoped) {
                    try {
                        Message message = callbackMessageQueue.take();
                        if (message != null) {
                            // load
                            List<Message> messageList = new ArrayList<>();
                            messageList.add(message);

                            List<Message> otherMessageList = new ArrayList<>();
                            int drainToNum = callbackMessageQueue.drainTo(otherMessageList, 100);
                            if (drainToNum > 0) {
                                messageList.addAll(otherMessageList);
                            }

                            // callback
                            broker.callbackMessages(messageList);
                        }
                    } catch (Exception e) {
                        if (!SmallMqClientFactory.clientFactoryPoolStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

                // finally total
                List<Message> otherMessageList = new ArrayList<>();
                int drainToNum = callbackMessageQueue.drainTo(otherMessageList);
                if (drainToNum > 0) {
                    broker.callbackMessages(otherMessageList);
                }
            });
        }
    }

    public void destoryBrokerService() throws Exception {
        // stop invoker factory
        if (rpcInvokerFactory != null) {
            rpcInvokerFactory.stop();
        }
    }

    // ---------------------- thread pool ----------------------
    private ExecutorService clientFactoryThreadPool = ThreadPoolUtil.makeServerThreadPool("small-mq-consumer", 10, 100);

    public static volatile boolean clientFactoryPoolStoped = false;

    /**
     * destory consumer thread
     */
    private void destoryClientFactoryThreadPool() {
        clientFactoryPoolStoped = true;
        clientFactoryThreadPool.shutdownNow();
    }

    // ---------------------- queue consumer ----------------------

    // queue consumer respository
    private List<ConsumerThread> consumerRespository = new ArrayList<>();

    private void startConsumer() {
        // valid
        if (consumerRespository == null || consumerRespository.size() == 0) {
            return;
        }
        // registry consumer
        getConsumerRegistryHelper().registerConsumer(consumerRespository);

        // execute thread
        for (ConsumerThread consumerThread : consumerRespository) {
            clientFactoryThreadPool.execute(consumerThread);
            logger.info(">>>>>>>>>>> small-mq, consumer init success, , topic:{}, group:{}",
                    consumerThread.getMqConsumer().topic(),
                    consumerThread.getMqConsumer().group());
        }
    }

    private void destoryConsumer() {
        // valid
        if (consumerRespository == null || consumerRespository.size() == 0) {
            return;
        }

        // stop registry consumer
        getConsumerRegistryHelper().removeConsumer(consumerRespository);
    }
}
