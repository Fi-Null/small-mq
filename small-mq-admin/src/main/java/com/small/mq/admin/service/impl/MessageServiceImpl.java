package com.small.mq.admin.service.impl;

import com.small.mq.admin.Result;
import com.small.mq.admin.dao.MessageDao;
import com.small.mq.admin.dao.MqTopicDao;
import com.small.mq.admin.model.MqBiz;
import com.small.mq.admin.service.MessageService;
import com.small.mq.admin.service.MqBizService;
import com.small.mq.client.consumer.annotation.Consumer;
import com.small.mq.client.message.Message;
import com.small.mq.client.message.MessageStatus;
import com.small.mq.client.util.DateUtil;
import com.small.mq.client.util.LogHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 1:53 PM
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageDao MessageDao;
    @Resource
    private MqBizService mqBizService;
    @Resource
    private MqTopicDao mqTopicDao;

    @Override
    public Map<String, Object> pageList(int offset, int pagesize, String topic, String status, Date addTimeStart, Date addTimeEnd) {

        List<Message> list = MessageDao.pageList(offset, pagesize, topic, status, addTimeStart, addTimeEnd);
        int total = MessageDao.pageListCount(offset, pagesize, topic, status, addTimeStart, addTimeEnd);

        Map<String, Object> maps = new HashMap<>();
        maps.put("data", list);
        maps.put("recordsTotal", total);
        maps.put("recordsFiltered", total);
        return maps;
    }

    @Override
    public Result<String> delete(int id) {
        int ret = MessageDao.delete(id);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

    @Override
    public Result<String> update(Message message) {

        // valid id
        if (message.getId() < 1) {
            return new Result<String>(500, "参数非法");
        }

        // valid message
        Result<String> validRet = validMessage(message);
        if (validRet != null) {
            return validRet;
        }

        // log
        String appendLog = LogHelper.makeLog("人工修改", message.toString());
        message.setLog(appendLog);

        // update
        int ret = MessageDao.update(message);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

    private static Result<String> validMessage(Message mqMessage) {

        if (mqMessage.getId() < 1) {    // add

            // topic
            if (mqMessage.getTopic() == null || mqMessage.getTopic().trim().length() == 0) {
                return new Result<String>(Result.FAIL_CODE, "small-mq, topic empty.");
            }
            if (!(mqMessage.getTopic().length() >= 4 && mqMessage.getTopic().length() <= 255)) {
                return new Result<String>(Result.FAIL_CODE, "small-mq, topic length invalid[4~255].");
            }

            // group
            if (mqMessage.getGroup() == null || mqMessage.getGroup().trim().length() == 0) {
                mqMessage.setGroup(Consumer.DEFAULT_GROUP);
            }
            if (!(mqMessage.getGroup().length() >= 4 && mqMessage.getGroup().length() <= 255)) {
                return new Result<String>(Result.FAIL_CODE, "small-mq, group length invalid[4~255].");
            }
        }

        // data
        if (mqMessage.getData() == null) {
            mqMessage.setData("");
        }
        if (mqMessage.getData().length() > 20000) {
            throw new IllegalArgumentException("small-mq, data length invalid[0~60000].");
        }

        // status
        //mqMessage.setStatus(MessageStatus.NEW.name());
        if (MessageStatus.valueOf(mqMessage.getStatus()) == null) {
            return new Result<String>(500, "消息状态非法");
        }

        // retryCount
        if (mqMessage.getRetryCount() < 0) {
            mqMessage.setRetryCount(0);
        }

        // shardingId
        if (mqMessage.getShardingId() < 0) {
            mqMessage.setShardingId(0);
        }

        // delayTime
        if (mqMessage.getEffectTime() == null) {
            mqMessage.setEffectTime(new Date());
        }

        // timeout
        if (mqMessage.getTimeout() < 0) {
            mqMessage.setTimeout(0);
        }

        // log

        return null;
    }

    @Override
    public Result<String> add(Message message) {

        // valid message
        Result<String> validRet = validMessage(message);
        if (validRet != null) {
            return validRet;
        }

        // log
        String appendLog = LogHelper.makeLog("人工添加", message.toString());
        message.setLog(appendLog);

        // save
        MessageDao.save(Arrays.asList(message));
        return Result.SUCCESS;
    }

    @Override
    public Map<String, Object> dashboardInfo() {

        int bizCount = 0;
        int topicCount = 0;
        int messageCount = 0;

        List<MqBiz> bizList = mqBizService.findAll();
        bizCount = bizList != null ? bizList.size() : 0;
        topicCount = mqTopicDao.pageListCount(0, 1, -1, null);
        messageCount = MessageDao.pageListCount(0, 1, null, null, null, null);

        Map<String, Object> dashboardMap = new HashMap<>();
        dashboardMap.put("bizCount", bizCount);
        dashboardMap.put("topicCount", topicCount);
        dashboardMap.put("messageCount", messageCount);
        return dashboardMap;
    }

    @Override
    public Result<Map<String, Object>> chartInfo(Date startDate, Date endDate) {

        // process
        List<String> messageDay_list = new ArrayList<String>();
        List<Integer> newNum_list = new ArrayList<Integer>();
        List<Integer> ingNum_list = new ArrayList<Integer>();
        List<Integer> successNum_list = new ArrayList<Integer>();
        List<Integer> failNum_list = new ArrayList<Integer>();

        int newNum_total = 0;
        int ingNum_total = 0;
        int successNum_total = 0;
        int failNum_total = 0;


        List<Map<String, Object>> triggerCountMapAll = MessageDao.messageCountByDay(startDate, endDate);
        if (triggerCountMapAll != null && triggerCountMapAll.size() > 0) {
            for (Map<String, Object> item : triggerCountMapAll) {

                String messageDay = String.valueOf(item.get("messageDay"));
                int newNum = Integer.valueOf(String.valueOf(item.get("newNum")));
                int ingNum = Integer.valueOf(String.valueOf(item.get("ingNum")));
                int successNum = Integer.valueOf(String.valueOf(item.get("successNum")));
                int failNum = Integer.valueOf(String.valueOf(item.get("failNum")));

                messageDay_list.add(messageDay);
                newNum_list.add(newNum);
                ingNum_list.add(ingNum);
                successNum_list.add(successNum);
                failNum_list.add(failNum);

                newNum_total += newNum;
                ingNum_total += ingNum;
                successNum_total += successNum;
                failNum_total += failNum;
            }
        } else {
            for (int i = 4; i > -1; i--) {
                String messageDay = DateUtil.formatDate(new Date());

                messageDay_list.add(messageDay);
                newNum_list.add(0);
                ingNum_list.add(0);
                successNum_list.add(0);
                failNum_list.add(0);
            }
        }


        Map<String, Object> result = new HashMap<>();
        result.put("messageDay_list", messageDay_list);
        result.put("newNum_list", newNum_list);
        result.put("ingNum_list", ingNum_list);
        result.put("successNum_list", successNum_list);
        result.put("failNum_list", failNum_list);

        result.put("newNum_total", newNum_total);
        result.put("ingNum_total", ingNum_total);
        result.put("successNum_total", successNum_total);
        result.put("failNum_total", failNum_total);

        return new Result<>(result);
    }

    @Override
    public Result<String> clearMessage(String topic, String status, int type) {
        MessageDao.clearMessage(topic, status, type);
        return Result.SUCCESS;
    }
}
