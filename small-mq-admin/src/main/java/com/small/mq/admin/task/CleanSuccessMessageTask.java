package com.small.mq.admin.task;

import com.small.mq.admin.dao.MessageDao;
import com.small.mq.admin.server.BrokerServer;
import com.small.mq.client.message.MessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 3:48 PM
 */
public class CleanSuccessMessageTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CleanSuccessMessageTask.class);

    @Resource
    private MessageDao messageDao;

    private BrokerServer brokerServer;

    public CleanSuccessMessageTask(BrokerServer brokerServer) {
        this.brokerServer = brokerServer;
    }

    @Override
    public void run() {
        boolean executorStoped = brokerServer.isExecutorStoped();
        int logretentiondays = brokerServer.getLogretentiondays();
        while (!executorStoped) {
            try {
                int count = messageDao.cleanSuccessMessage(MessageStatus.SUCCESS.name(), logretentiondays);
                logger.info("small-mq, clean success message, count:{}", count);
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
            try {
                TimeUnit.DAYS.sleep(logretentiondays);
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
