package com.small.mq.admin.controller;

import com.small.mq.admin.Result;
import com.small.mq.admin.model.MqBiz;
import com.small.mq.admin.model.MqTopic;
import com.small.mq.admin.service.MqBizService;
import com.small.mq.admin.service.MqTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 2:11 PM
 */
@Controller
@RequestMapping("/topic")
public class TopicController {

    @Resource
    private MqTopicService mqTopicService;
    @Resource
    private MqBizService mqBizService;


    @RequestMapping("")
    public String index(Model model) {

        List<MqBiz> bizList = mqBizService.findAll();
        model.addAttribute("bizList", bizList);

        return "topic/topic.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        int bizId,
                                        String topic) {
        return mqTopicService.pageList(start, length, bizId, topic);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Result<String> delete(String topic) {
        return mqTopicService.delete(topic);
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result<String> update(MqTopic mqTopic) {
        return mqTopicService.update(mqTopic);
    }

    @RequestMapping("/add")
    @ResponseBody
    public Result<String> add(MqTopic mqTopic) {
        return mqTopicService.add(mqTopic);
    }
}
