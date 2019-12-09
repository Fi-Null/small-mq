package com.small.mq.admin.task;

import com.small.mq.admin.dao.MessageDao;
import com.small.mq.admin.server.BrokerServer;
import com.small.mq.client.message.Message;
import com.small.mq.client.message.MessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 3:10 PM
 */
@Component
public class UpdateMessageTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(UpdateMessageTask.class);

    @Resource
    private MessageDao messageDao;

    private BrokerServer brokerServer;

    public UpdateMessageTask(BrokerServer brokerServer) {
        this.brokerServer = brokerServer;
    }

    @Override
    public void run() {
        boolean executorStoped = brokerServer.isExecutorStoped();
        LinkedBlockingQueue<Message> callbackMessageQueue = brokerServer.getCallbackMessageQueue();
        Map<String, Long> alarmMessageInfo = brokerServer.getAlarmMessageInfo();

        while (!executorStoped) {
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

                    // save
                    messageDao.updateStatus(messageList);

                    // fill alarm info
                    for (Message alarmItem : messageList) {
                        if (MessageStatus.FAIL.name().equals(alarmItem.getStatus())) {
                            Long failCount = alarmMessageInfo.get(alarmItem.getTopic());
                            failCount = failCount != null ? failCount++ : 1;
                            alarmMessageInfo.put(alarmItem.getTopic(), failCount);
                        }
                    }

                }

            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        // end save
        List<Message> otherMessageList = new ArrayList<>();
        int drainToNum = callbackMessageQueue.drainTo(otherMessageList);
        if (drainToNum > 0) {
            messageDao.updateStatus(otherMessageList);
        }
    }
}
