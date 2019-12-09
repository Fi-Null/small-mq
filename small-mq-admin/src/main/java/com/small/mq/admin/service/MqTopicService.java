package com.small.mq.admin.service;

import com.small.mq.admin.Result;
import com.small.mq.admin.model.MqTopic;

import java.util.Map;

public interface MqTopicService {

    Map<String, Object> pageList(int start, int length, int bizId, String topic);

    MqTopic load(String topic);

    Result<String> add(MqTopic MqTopic);

    Result<String> update(MqTopic MqTopic);

    Result<String> delete(String topic);

}
