package com.small.mq.admin.controller.interceptor;

import com.small.mq.admin.controller.annotation.PermessionLimit;
import com.small.mq.admin.util.CookieUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 11:27 AM
 */
@Component
public class PermissionInterceptor extends HandlerInterceptorAdapter implements InitializingBean {


    // ---------------------- init ----------------------

    @Value("${small.mq.login.username}")
    private String username;
    @Value("${small.mq.login.password}")
    private String password;

    @Override
    public void afterPropertiesSet() throws Exception {

        // valid
        if (username == null || username.trim().length() == 0 || password == null || password.trim().length() == 0) {
            throw new RuntimeException("权限账号密码不可为空");
        }

        // login token
        String tokenTmp = DigestUtils.md5DigestAsHex(String.valueOf(username + "_" + password).getBytes());        //.getBytes("UTF-8")
        tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

        LOGIN_IDENTITY_TOKEN = tokenTmp;
    }

    // ---------------------- tool ----------------------

    public static final String LOGIN_IDENTITY_KEY = "SMALL_MQ_LOGIN_IDENTITY";
    private static String LOGIN_IDENTITY_TOKEN;

    public static String getLoginIdentityToken() {
        return LOGIN_IDENTITY_TOKEN;
    }

    public static boolean login(HttpServletResponse response, String username, String password, boolean ifRemember) {

        // login token
        String tokenTmp = DigestUtils.md5DigestAsHex(String.valueOf(username + "_" + password).getBytes());
        tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

        if (!getLoginIdentityToken().equals(tokenTmp)) {
            return false;
        }

        // do login
        CookieUtil.set(response, LOGIN_IDENTITY_KEY, getLoginIdentityToken(), ifRemember);
        return true;
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
    }

    public static boolean ifLogin(HttpServletRequest request) {
        String indentityInfo = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
        if (indentityInfo == null || !getLoginIdentityToken().equals(indentityInfo.trim())) {
            return false;
        }
        return true;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return super.preHandle(request, response, handler);
        }

        if (!ifLogin(request)) {
            HandlerMethod method = (HandlerMethod) handler;
            PermessionLimit permission = method.getMethodAnnotation(PermessionLimit.class);
            if (permission == null || permission.limit()) {
                response.sendRedirect(request.getContextPath() + "/toLogin");
                //request.getRequestDispatcher("/toLogin").forward(request, response);
                return false;
            }
        }

        return super.preHandle(request, response, handler);
    }

}
