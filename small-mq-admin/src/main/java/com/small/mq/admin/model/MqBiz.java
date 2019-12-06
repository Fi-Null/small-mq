package com.small.mq.admin.model;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/6/19 4:32 PM
 */
public class MqBiz {
    private int id;             // 业务线ID
    private String bizName;     // 业务线名称
    private int order;          // 顺序

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
