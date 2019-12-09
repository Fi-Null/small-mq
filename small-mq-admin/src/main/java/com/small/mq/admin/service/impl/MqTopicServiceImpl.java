package com.small.mq.admin.service.impl;

import com.small.mq.admin.Result;
import com.small.mq.admin.dao.MessageDao;
import com.small.mq.admin.dao.MqTopicDao;
import com.small.mq.admin.model.MqTopic;
import com.small.mq.admin.service.MqTopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 1:48 PM
 */

@Service
public class MqTopicServiceImpl implements MqTopicService {

    private static Logger logger = LoggerFactory.getLogger(MqTopicServiceImpl.class);

    @Resource
    private MqTopicDao mqTopicDao;
    @Resource
    private MessageDao messageDao;


    @Override
    public Map<String, Object> pageList(int start, int length, int bizId, String topic) {
        // page list
        List<MqTopic> list = mqTopicDao.pageList(start, length, bizId, topic);
        int list_count = mqTopicDao.pageListCount(start, length, bizId, topic);

        // package result
        Map<String, Object> maps = new HashMap<>();
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);    // 过滤后的总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    @Override
    public MqTopic load(String topic) {
        return mqTopicDao.load(topic);
    }

    @Override
    public Result<String> add(MqTopic MqTopic) {

        // valid
        if (MqTopic.getTopic() == null || MqTopic.getTopic().trim().length() == 0) {
            return new Result<>(Result.FAIL_CODE, "消息主题不可为空");
        }
        if (!(MqTopic.getTopic().length() >= 4 && MqTopic.getTopic().length() <= 255)) {
            return new Result<>(Result.FAIL_CODE, "消息主题长度非法[4~255]");
        }

        // exist
        MqTopic exist = mqTopicDao.load(MqTopic.getTopic());
        if (exist != null) {
            return new Result<>(Result.FAIL_CODE, "消息主题不可重复");
        }


        int ret = mqTopicDao.add(MqTopic);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }


    @Override
    public Result<String> update(MqTopic MqTopic) {

        // valid
        if (MqTopic.getTopic() == null || MqTopic.getTopic().trim().length() == 0) {
            return new Result<>(Result.FAIL_CODE, "消息主题不可为空");
        }
        if (!(MqTopic.getTopic().length() >= 4 && MqTopic.getTopic().length() <= 255)) {
            return new Result<>(Result.FAIL_CODE, "消息主题长度非法[4~255]");
        }

        int ret = mqTopicDao.update(MqTopic);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

    @Override
    public Result<String> delete(String topic) {

        // valid, limit use
        int count = messageDao.pageListCount(0, 1, topic, null, null, null);
        if (count > 0) {
            return new Result<>(Result.FAIL_CODE, "禁止删除，该Topic下存在消息");
        }

        int ret = mqTopicDao.delete(topic);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

}
