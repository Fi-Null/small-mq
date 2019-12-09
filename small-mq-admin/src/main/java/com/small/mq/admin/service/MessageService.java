package com.small.mq.admin.service;

import com.small.mq.admin.Result;
import com.small.mq.client.message.Message;

import java.util.Date;
import java.util.Map;

public interface MessageService {

    Map<String, Object> pageList(int offset, int pagesize, String topic, String status, Date addTimeStart, Date addTimeEnd);

    Result<String> delete(int id);

    Result<String> update(Message message);

    Result<String> add(Message message);

    Map<String, Object> dashboardInfo();

    Result<Map<String, Object>> chartInfo(Date startDate, Date endDate);

    Result<String> clearMessage(String topic, String status, int type);

}
