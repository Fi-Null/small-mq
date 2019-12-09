package com.small.mq.admin.controller.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 11:25 AM
 */
@Component
public class CookieInterceptor extends HandlerInterceptorAdapter {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && request.getCookies() != null && request.getCookies().length > 0) {
            HashMap<String, Cookie> cookieMap = new HashMap<>();
            for (Cookie ck : request.getCookies()) {
                cookieMap.put(ck.getName(), ck);
            }
            modelAndView.addObject("cookieMap", cookieMap);
        }

        super.postHandle(request, response, handler, modelAndView);
    }

}