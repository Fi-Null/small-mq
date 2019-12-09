package com.small.mq.admin.task;

import com.small.mq.admin.dao.MqTopicDao;
import com.small.mq.admin.model.MqTopic;
import com.small.mq.admin.server.BrokerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 3:34 PM
 */
@Component
public class AlarmTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(SaveMessageTask.class);

    @Resource
    private MqTopicDao mqTopicDao;

    private BrokerServer brokerServer;

    public AlarmTask(BrokerServer brokerServer) {
        this.brokerServer = brokerServer;
    }

    @Override
    public void run() {

        boolean executorStoped = brokerServer.isExecutorStoped();
        Map<String, Long> alarmMessageInfo = brokerServer.getAlarmMessageInfo();
        JavaMailSender mailSender = brokerServer.getMailSender();
        String emailUserName = brokerServer.getEmailUserName();

        while (!executorStoped) {
            try {
                // mult send alarm
                if (alarmMessageInfo.size() > 0) {

                    // copy
                    Map<String, Long> alarmMessageInfoTmp = new HashMap<>();
                    alarmMessageInfoTmp.putAll(alarmMessageInfo);
                    alarmMessageInfo.clear();

                    // alarm
                    List<MqTopic> topicList = mqTopicDao.findAlarmByTopic(new ArrayList<>(alarmMessageInfoTmp.keySet()));
                    if (topicList != null && topicList.size() > 0) {
                        for (MqTopic mqTopic : topicList) {
                            if (mqTopic.getAlarmEmails() != null && mqTopic.getAlarmEmails().trim().length() > 0) {
                                Long failCount = alarmMessageInfoTmp.get(mqTopic.getTopic());

                                String[] toEmailList = null;
                                if (mqTopic.getAlarmEmails().contains(",")) {
                                    toEmailList = mqTopic.getAlarmEmails().split(",");
                                } else {
                                    toEmailList = new String[]{mqTopic.getAlarmEmails()};
                                }
                                String emailContent = MessageFormat.format("告警类型：消息失败；<br>Topic：{0}；<br>备注：{1}",
                                        mqTopic.getTopic(), "1min内失败消息数量=" + failCount);

                                // make mail
                                try {
                                    MimeMessage mimeMessage = mailSender.createMimeMessage();

                                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                                    helper.setFrom(emailUserName, "分布式消息队列small-mq");
                                    helper.setTo(toEmailList);
                                    helper.setSubject("消息队列中心监控报警");
                                    helper.setText(emailContent, true);

                                    mailSender.send(mimeMessage);
                                } catch (Exception e) {
                                    logger.error(">>>>>>>>>>> message monitor alarm email send error, topic:{}, failCount:{}", mqTopic.getTopic(), failCount);
                                }


                                // TODO, custom alarm strategy, such as sms
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
            try {
                // sleep
                TimeUnit.MINUTES.sleep(1);
            } catch (Exception e) {
                if (!executorStoped) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
