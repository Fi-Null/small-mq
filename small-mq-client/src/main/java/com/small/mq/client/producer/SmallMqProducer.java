package com.small.mq.client.producer;

import com.small.mq.client.consumer.annotation.SmallMqConsumer;
import com.small.mq.client.factory.SmallMqClientFactory;
import com.small.mq.client.message.MessageStatus;
import com.small.mq.client.message.SmallMqMessage;
import com.small.mq.client.util.IpUtil;
import com.small.mq.client.util.LogHelper;

import java.util.Date;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 3:24 PM
 */
public class SmallMqProducer {

    // ---------------------- valid message ----------------------

    /**
     * valid message
     *
     * @param mqMessage
     * @return
     */
    public static void validMessage(SmallMqMessage mqMessage) {
        if (mqMessage == null) {
            throw new IllegalArgumentException("small-mq, SmallMqMessage can not be null.");
        }

        // topic
        if (mqMessage.getTopic() == null || mqMessage.getTopic().trim().length() == 0) {
            throw new IllegalArgumentException("small-mq, topic empty.");
        }
        if (!(mqMessage.getTopic().length() >= 4 && mqMessage.getTopic().length() <= 255)) {
            throw new IllegalArgumentException("small-mq, topic length invalid[4~255].");
        }

        // group
        if (mqMessage.getGroup() == null || mqMessage.getGroup().trim().length() == 0) {
            mqMessage.setGroup(SmallMqConsumer.DEFAULT_GROUP);
        }
        if (!(mqMessage.getGroup().length() >= 4 && mqMessage.getGroup().length() <= 255)) {
            throw new IllegalArgumentException("small-mq, group length invalid[4~255].");
        }

        // data
        if (mqMessage.getData() == null) {
            mqMessage.setData("");
        }
        if (mqMessage.getData().length() > 20000) {
            throw new IllegalArgumentException("small-mq, data length invalid[0~60000].");
        }

        // status
        mqMessage.setStatus(MessageStatus.NEW.name());

        // retryCount
        if (mqMessage.getRetryCount() < 0) {
            mqMessage.setRetryCount(0);
        }

        // shardingId
        if (mqMessage.getShardingId() < 0) {
            mqMessage.setShardingId(0);
        }

        // delayTime
        if (mqMessage.getEffectTime() == null) {
            mqMessage.setEffectTime(new Date());
        }

        // timeout
        if (mqMessage.getTimeout() < 0) {
            mqMessage.setTimeout(0);
        }

        // log
        String appendLog = LogHelper.makeLog("生产消息", "消息生产者IP=" + IpUtil.getIp());
        mqMessage.setLog(appendLog);
    }


    // ---------------------- produce message ----------------------

    /**
     * produce produce
     */
    public static void produce(SmallMqMessage mqMessage, boolean async) {
        // valid
        validMessage(mqMessage);

        // send
        SmallMqClientFactory.addMessages(mqMessage, async);
    }

    public static void produce(SmallMqMessage mqMessage) {
        produce(mqMessage, true);
    }


    // ---------------------- broadcast message ----------------------

    /**
     * broadcast produce
     */
    public static void broadcast(SmallMqMessage mqMessage, boolean async) {
        // valid
        validMessage(mqMessage);

        // find online group
//        Set<String> groupList = SmallMqClientFactory.getConsumerRegistryHelper().getTotalGroupList(mqMessage.getTopic());
//
//        // broud total online group
//        for (String group : groupList) {
//
//            // clone msg
//            SmallMqMessage cloneMsg = new SmallMqMessage(mqMessage);
//            cloneMsg.setGroup(group);
//
//            // produce clone msg
//            produce(cloneMsg, true);
//        }
    }

    public static void broadcast(SmallMqMessage mqMessage) {
        broadcast(mqMessage, true);
    }

}
