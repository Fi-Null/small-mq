package com.small.mq.client.factory;

import com.small.mq.client.broker.SmallMqBroker;
import com.small.mq.client.consumer.annotation.SmallMqConsumer;
import com.small.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.small.mq.client.message.SmallMqMessage;
import com.small.mq.client.util.ThreadPoolUtil;
import com.small.rpc.remoting.invoker.RpcInvokerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
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
    private List<SmallMqConsumer> consumerList;

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setConsumerList(List<SmallMqConsumer> consumerList) {
        this.consumerList = consumerList;
    }

    // ---------------------- init destroy  ----------------------

   /* public void init() {

        // pre : valid consumer
        validConsumer();

        // start BrokerService
        startBrokerService();

        // start consumer
        startConsumer();
    }*/



    private RpcInvokerFactory rpcInvokerFactory = null;

    private static SmallMqBroker broker;
    private static ConsumerRegistryHelper consumerRegistryHelper = null;
    private static LinkedBlockingQueue<SmallMqMessage> newMessageQueue = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<SmallMqMessage> callbackMessageQueue = new LinkedBlockingQueue<>();


    public static ConsumerRegistryHelper getConsumerRegistryHelper() {
        return consumerRegistryHelper;
    }

    public static SmallMqBroker getBroker() {
        return broker;
    }

    public static void addMessages(SmallMqMessage mqMessage, boolean async) {
        if (async) {
            // async queue, mult send
            newMessageQueue.add(mqMessage);
        } else {
            // sync rpc, one send
            broker.addMessages(Arrays.asList(mqMessage));
        }
    }

    public static void callbackMessage(SmallMqMessage mqMessage){
        callbackMessageQueue.add(mqMessage);
    }



    // ---------------------- thread pool ----------------------
    private ExecutorService clientFactoryThreadPool = ThreadPoolUtil.makeServerThreadPool("small-mq-consumer", 10, 100);
    public static volatile boolean clientFactoryPoolStoped = false;



}
