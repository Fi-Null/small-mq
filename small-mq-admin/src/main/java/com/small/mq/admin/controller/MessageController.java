package com.small.mq.admin.controller;

import com.small.mq.admin.Result;
import com.small.mq.admin.service.MessageService;
import com.small.mq.client.message.Message;
import com.small.mq.client.message.MessageStatus;
import com.small.mq.client.util.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 2:07 PM
 */
@Controller
@RequestMapping("/message")
public class MessageController {
    @Resource
    private MessageService messageService;

    @RequestMapping("")
    public String index(Model model, String topic) {

        model.addAttribute("status", MessageStatus.values());
        model.addAttribute("topic", topic);

        return "message/message.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String topic,
                                        String status,
                                        String filterTime) {

        // parse param
        Date addTimeStart = null;
        Date addTimeEnd = null;
        if (filterTime != null && filterTime.trim().length() > 0) {
            String[] temp = filterTime.split(" - ");
            if (temp != null && temp.length == 2) {
                addTimeStart = DateUtil.parseDateTime(temp[0]);
                addTimeEnd = DateUtil.parseDateTime(temp[1]);
            }
        }

        return messageService.pageList(start, length, topic, status, addTimeStart, addTimeEnd);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Result<String> delete(int id) {
        return messageService.delete(id);
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result<String> update(long id,
                                 String topic,
                                 String group,
                                 String data,
                                 String status,
                                 @RequestParam(required = false, defaultValue = "0") int retryCount,
                                 @RequestParam(required = false, defaultValue = "0") long shardingId,
                                 @RequestParam(required = false, defaultValue = "0") int timeout,
                                 String effectTime) {

        // effectTime
        Date effectTimeObj = null;
        if (effectTime != null && effectTime.trim().length() > 0) {
            effectTimeObj = DateUtil.parseDateTime(effectTime);
            if (effectTimeObj == null) {
                return new Result<String>(Result.FAIL_CODE, "生效时间格式非法");
            }
        }

        // message
        Message message = new Message();
        message.setId(id);
        message.setTopic(topic);
        message.setGroup(group);
        message.setData(data);
        message.setStatus(status);
        message.setRetryCount(retryCount);
        message.setShardingId(shardingId);
        message.setTimeout(timeout);
        message.setEffectTime(effectTimeObj);

        return messageService.update(message);
    }

    @RequestMapping("/add")
    @ResponseBody
    public Result<String> add(String topic,
                              String group,
                              String data,
                              String status,
                              @RequestParam(required = false, defaultValue = "0") int retryCount,
                              @RequestParam(required = false, defaultValue = "0") long shardingId,
                              @RequestParam(required = false, defaultValue = "0") int timeout,
                              String effectTime) {

        // effectTime
        Date effectTimeObj = null;
        if (effectTime != null && effectTime.trim().length() > 0) {
            effectTimeObj = DateUtil.parseDateTime(effectTime);
            if (effectTimeObj == null) {
                return new Result<String>(Result.FAIL_CODE, "生效时间格式非法");
            }
        }

        // message
        Message message = new Message();
        message.setTopic(topic);
        message.setGroup(group);
        message.setData(data);
        message.setStatus(status);
        message.setRetryCount(retryCount);
        message.setShardingId(shardingId);
        message.setTimeout(timeout);
        message.setEffectTime(effectTimeObj);

        return messageService.add(message);
    }

    @RequestMapping("/clearMessage")
    @ResponseBody
    public Result<String> clearMessage(String topic, String status, int type) {
        return messageService.clearMessage(topic, status, type);
    }

}
