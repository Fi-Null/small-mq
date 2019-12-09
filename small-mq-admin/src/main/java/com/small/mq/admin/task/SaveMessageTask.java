package com.small.mq.admin.task;

import com.small.mq.admin.dao.MessageDao;
import com.small.mq.admin.server.BrokerServer;
import com.small.mq.client.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 2:55 PM
 */
@Component
public class SaveMessageTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(SaveMessageTask.class);

    @Resource
    private MessageDao messageDao;

    private BrokerServer brokerServer;

    public SaveMessageTask(BrokerServer brokerServer) {
        this.brokerServer = brokerServer;
    }

    @Override
    public void run() {

        boolean executorStoped = brokerServer.isExecutorStoped();
        LinkedBlockingQueue<Message> newMessageQueue = brokerServer.getNewMessageQueue();

        while (!executorStoped) {
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
                    messageDao.save(messageList);
                }
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        // end save
        List<Message> otherMessageList = new ArrayList<>();
        int drainToNum = newMessageQueue.drainTo(otherMessageList);
        if (drainToNum > 0) {
            messageDao.save(otherMessageList);
        }
    }
}
