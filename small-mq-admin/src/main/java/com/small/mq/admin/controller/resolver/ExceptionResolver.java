package com.small.mq.admin.controller.resolver;

import com.small.mq.admin.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 11:33 AM
 */
@Component
public class ExceptionResolver implements HandlerExceptionResolver {

    private static transient Logger logger = LoggerFactory.getLogger(ExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {
        logger.error("WebExceptionResolver:{}", ex);

        // if json
        boolean isJson = false;
        HandlerMethod method = (HandlerMethod) handler;
        ResponseBody responseBody = method.getMethodAnnotation(ResponseBody.class);
        if (responseBody != null) {
            isJson = true;
        }

        // error result
        Result<String> errorResult = new Result<>(Result.FAIL_CODE, ex.toString().replaceAll("\n", "<br/>"));

        // response
        ModelAndView mv = new ModelAndView();
        if (isJson) {
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().print("{\"code\":" + errorResult.getCode() + ", \"msg\":\"" + errorResult.getMsg() + "\"}");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return mv;
        } else {

            mv.addObject("exceptionMsg", errorResult.getMsg());
            mv.setViewName("/common/common.exception");
            return mv;
        }
    }
}
