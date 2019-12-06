package com.small.mq.admin.model;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/6/19 4:31 PM
 */
public class MqTopic {

    private Long id;                //自增id
    private String topic;           // 消息主题
    private int bizId;              // 业务线ID
    private String author;          // 负责人
    private String alarmEmails;     // 告警邮箱，多个逗号分隔；窗口期5min，只检验窗口期数据，存在消息阻塞（公共阻塞阈值），失败消息（公共失败率阈值）时，5min推送告警一次；

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getBizId() {
        return bizId;
    }

    public void setBizId(int bizId) {
        this.bizId = bizId;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlarmEmails() {
        return alarmEmails;
    }

    public void setAlarmEmails(String alarmEmails) {
        this.alarmEmails = alarmEmails;
    }

}
