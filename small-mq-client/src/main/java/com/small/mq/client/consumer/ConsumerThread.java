package com.small.mq.client.consumer;

import com.small.mq.client.consumer.annotation.SmallMqConsumer;
import com.small.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.small.mq.client.factory.SmallMqClientFactory;
import com.small.mq.client.message.MessageStatus;
import com.small.mq.client.message.SmallMqMessage;
import com.small.mq.client.util.IpUtil;
import com.small.mq.client.util.LogHelper;
import com.small.mq.client.util.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 6:22 PM
 */
public class ConsumerThread extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

    private SmallMqConsumerListener consumerHandler;

    private SmallMqConsumer mqConsumer;

    private String uuid;

    public ConsumerThread(SmallMqConsumerListener consumerHandler) {
        this.consumerHandler = consumerHandler;

        this.mqConsumer = consumerHandler.getClass().getAnnotation(SmallMqConsumer.class);

        this.uuid = UUID.randomUUID().toString().replaceAll("-", "");
    }

    public SmallMqConsumer getMqConsumer() {
        return mqConsumer;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public void run() {

        int waitTim = 0;

        while (!SmallMqClientFactory.clientFactoryPoolStoped) {
            try {
                // check active
                ConsumerRegistryHelper.ActiveInfo activeInfo = SmallMqClientFactory.getConsumerRegistryHelper().isActice(this);
                logger.debug(">>>>>>>>>>> small-mq, consumer active check, topic:{}, group:{}, ActiveInfo={}", mqConsumer.topic(), mqConsumer.group(), activeInfo);

                if (activeInfo != null) {

                    // pullNewMessage
                    List<SmallMqMessage> messageList = SmallMqClientFactory.getBroker().pullNewMessage(mqConsumer.topic(), mqConsumer.group(), activeInfo.rank, activeInfo.total, 100);
                    if (messageList != null && messageList.size() > 0) {

                        // reset wait time
                        if (mqConsumer.transaction()) {
                            waitTim = 0;    // transaction message status timely updated by lock, will not repeat pull
                        } else {
                            waitTim = 1;    // no-transaction message status delay updated by callback, may be repeat, need wail for callback
                        }

                        for (final SmallMqMessage msg : messageList) {

                            // check active twice
                            ConsumerRegistryHelper.ActiveInfo newActiveInfo = SmallMqClientFactory.getConsumerRegistryHelper().isActice(this);
                            if (!(newActiveInfo != null && newActiveInfo.rank == activeInfo.rank && newActiveInfo.total == activeInfo.total)) {
                                break;
                            }

                            // lock message, for transaction
                            if (mqConsumer.transaction()) {
                                String appendLog_lock = LogHelper.makeLog(
                                        "锁定消息",
                                        ("消费者信息=" + newActiveInfo.toString()
                                                + "；<br>消费者IP=" + IpUtil.getIp())
                                );
                                int lockRet = SmallMqClientFactory.getBroker().lockMessage(msg.getId(), appendLog_lock);
                                if (lockRet < 1) {
                                    continue;
                                }
                            }

                            // consume message
                            SmallMqResult smallMqResult = null;
                            try {

                                if (msg.getTimeout() > 0) {
                                    // limit timeout
                                    Thread futureThread = null;
                                    try {
                                        FutureTask<SmallMqResult> futureTask = new FutureTask<>(
                                                () -> consumerHandler.consume(msg.getData()));
                                        futureThread = new Thread(futureTask);
                                        futureThread.start();

                                        smallMqResult = futureTask.get(msg.getTimeout(), TimeUnit.SECONDS);
                                    } catch (TimeoutException e) {
                                        logger.error(e.getMessage(), e);
                                        smallMqResult = new SmallMqResult(SmallMqResult.FAIL_CODE, "Timeout:" + e.getMessage());
                                    } finally {
                                        futureThread.interrupt();
                                    }
                                } else {
                                    // direct run
                                    smallMqResult = consumerHandler.consume(msg.getData());
                                }

                                if (smallMqResult == null) {
                                    smallMqResult = SmallMqResult.FAIL;
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                String errorMsg = ThrowableUtil.toString(e);
                                smallMqResult = new SmallMqResult(SmallMqResult.FAIL_CODE, errorMsg);
                            }

                            // log
                            String appendLog_consume = null;
                            if (mqConsumer.transaction()) {
                                appendLog_consume = LogHelper.makeLog(
                                        "消费消息",
                                        ("消费结果=" + (smallMqResult.isSuccess() ? "成功" : "失败")
                                                + "；<br>消费日志=" + smallMqResult.getLog())
                                );
                            } else {
                                appendLog_consume = LogHelper.makeLog(
                                        "消费消息",
                                        ("消费结果=" + (smallMqResult.isSuccess() ? "成功" : "失败")
                                                + "；<br>消费者信息=" + activeInfo.toString()
                                                + "；<br>消费者IP=" + IpUtil.getIp()
                                                + "；<br>消费日志=" + smallMqResult.getLog())
                                );
                            }

                            // callback
                            msg.setStatus(smallMqResult.isSuccess() ? MessageStatus.SUCCESS.name() : MessageStatus.FAIL.name());
                            msg.setLog(appendLog_consume);
                            SmallMqClientFactory.callbackMessage(msg);

                            logger.info(">>>>>>>>>>> small-mq, consumer finish,  topic:{}, group:{}, ActiveInfo={}", mqConsumer.topic(), mqConsumer.group(), activeInfo.toString());
                        }

                    } else {
                        waitTim = (waitTim + 10) <= 60 ? (waitTim + 10) : 60;
                    }
                } else {
                    waitTim = 2;
                }

            } catch (Exception e) {
                if (!SmallMqClientFactory.clientFactoryPoolStoped) {
                    logger.error(e.getMessage(), e);
                }
            }

            // wait
            try {
                TimeUnit.SECONDS.sleep(waitTim);
            } catch (Exception e) {
                if (!SmallMqClientFactory.clientFactoryPoolStoped) {
                    logger.error(e.getMessage(), e);
                }
            }

        }
    }

}
