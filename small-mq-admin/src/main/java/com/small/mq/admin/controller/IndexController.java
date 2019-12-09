package com.small.mq.admin.controller;

import com.small.mq.admin.Result;
import com.small.mq.admin.controller.annotation.PermessionLimit;
import com.small.mq.admin.controller.interceptor.PermissionInterceptor;
import com.small.mq.admin.service.MessageService;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 2:05 PM
 */
@Controller
public class IndexController {

    @Resource
    private MessageService messageService;

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request) {

        Map<String, Object> dashboardMap = messageService.dashboardInfo();
        model.addAllAttributes(dashboardMap);

        return "index";
    }

    @RequestMapping("/chartInfo")
    @ResponseBody
    public Result<Map<String, Object>> chartInfo(Date startDate, Date endDate) {
        Result<Map<String, Object>> chartInfo = messageService.chartInfo(startDate, endDate);
        return chartInfo;
    }


    @RequestMapping("/toLogin")
    @PermessionLimit(limit = false)
    public String toLogin(Model model, HttpServletRequest request) {
        if (PermissionInterceptor.ifLogin(request)) {
            return "redirect:/";
        }
        return "login";
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit = false)
    public Result<String> loginDo(HttpServletRequest request, HttpServletResponse response, String userName, String password, String ifRemember) {
        // valid
        if (PermissionInterceptor.ifLogin(request)) {
            return Result.SUCCESS;
        }

        // param
        if (userName == null || userName.trim().length() == 0 || password == null || password.trim().length() == 0) {
            return new Result<String>(500, "请输入账号密码");
        }
        boolean ifRem = (ifRemember != null && "on".equals(ifRemember)) ? true : false;

        // do login
        boolean loginRet = PermissionInterceptor.login(response, userName, password, ifRem);
        if (!loginRet) {
            return new Result<String>(500, "账号密码错误");
        }
        return Result.SUCCESS;
    }

    @RequestMapping(value = "logout", method = RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit = false)
    public Result<String> logout(HttpServletRequest request, HttpServletResponse response) {
        if (PermissionInterceptor.ifLogin(request)) {
            PermissionInterceptor.logout(request, response);
        }
        return Result.SUCCESS;
    }

    @RequestMapping("/help")
    public String help() {
        return "help";
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}
