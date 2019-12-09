package com.small.mq.admin.controller;

import com.small.mq.admin.Result;
import com.small.mq.admin.model.MqBiz;
import com.small.mq.admin.service.MqBizService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 11:36 AM
 */
@Controller
@RequestMapping("/biz")
public class BizController {

    @Resource
    private MqBizService mqBizService;

    @RequestMapping("")
    public String index(Model model) {

        List<MqBiz> bizList = mqBizService.findAll();
        model.addAttribute("bizList", bizList);

        return "biz/biz.index";
    }

    @RequestMapping("/save")
    @ResponseBody
    public Result<String> save(MqBiz MqBiz) {
        return mqBizService.add(MqBiz);
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result<String> update(MqBiz MqBiz) {
        return mqBizService.update(MqBiz);
    }


    @RequestMapping("/remove")
    @ResponseBody
    public Result<String> remove(int id) {
        return mqBizService.delete(id);
    }
}
