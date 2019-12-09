package com.small.mq.admin.task;

import com.small.mq.admin.dao.MessageDao;
import com.small.mq.admin.server.BrokerServer;
import com.small.mq.client.message.MessageStatus;
import com.small.mq.client.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 3:32 PM
 */
@Component
public class ResetBlockTimeOutMessageTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ResetBlockTimeOutMessageTask.class);

    @Resource
    private MessageDao messageDao;

    private BrokerServer brokerServer;

    public ResetBlockTimeOutMessageTask(BrokerServer brokerServer) {
        this.brokerServer = brokerServer;
    }

    @Override
    public void run() {
        boolean executorStoped = brokerServer.isExecutorStoped();

        while (!executorStoped) {
            try {
                // mult retry message
                String appendLog = LogHelper.makeLog("失败重试", "状态自动还原,剩余重试次数减一");
                int count = messageDao.updateRetryCount(MessageStatus.FAIL.name(), MessageStatus.NEW.name(), appendLog);
                if (count > 0) {
                    logger.info("small-mq, retry message, count:{}", count);
                }
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
            try {
                // mult reset block message
                String appendLog = LogHelper.makeLog("阻塞清理", "状态自动标记失败");
                int count = messageDao.resetBlockTimeoutMessage(MessageStatus.RUNNING.name(), MessageStatus.FAIL.name(), appendLog);
                if (count > 0) {
                    logger.info("small-mq, retry block message, count:{}", count);
                }
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
            try {
                // sleep
                TimeUnit.SECONDS.sleep(60);
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
