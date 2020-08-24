package com.u8x.dao.config;

import com.alibaba.druid.support.json.JSONUtils;
import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.controller.AdminController;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 重写onAccessDenied方法， 如果是ajax请求， 那么返回给客户端统一的【需要重新登录状态,code:2】
 * Created by ant on 2017/5/30.
 */
public class ShiroCustomFilter extends FormAuthenticationFilter{

    private static final XLogger log = XLogger.getLogger(ShiroCustomFilter.class);

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        HttpServletResponse httpServletResponse = (HttpServletResponse)response;

        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                if (log.isTraceEnabled()) {
                    log.trace("Login submission detected.  Attempting to execute login.");
                }
                return executeLogin(request, response);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Login page view.");
                }
                //allow them to see the login page ;)
                return true;
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Attempting to access a path which requires authentication.  Forwarding to the " +
                        "Authentication url [" + getLoginUrl() + "]");
            }

            //如果是Ajax请求，不跳转登录
            if (isAjaxRequest(httpServletRequest)){
                log.debug("ajax request without login. please login first. url:"+request.getRemoteAddr());
                //httpServletResponse.setStatus(401);
                responseReLogin(httpServletResponse);

            } else {
                saveRequestAndRedirectToLogin(request, response);
            }
            return false;
        }
    }

    //判断当前请求是否为ajax请求
    public boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("x-requested-with");
        if (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest")) {
            return true;
        } else {
            return false;
        }
    }

    private void responseReLogin(HttpServletResponse response)
    {
        String json = String.format("{\"code\":%d,\"reason\":\"%s\"}", Consts.RespCode.ReLogin, Consts.Tips.SessionInvalid);
        render(response, json, "text/json; charset=UTF-8");
    }

    private void render(HttpServletResponse response, String content, String contentType)
    {
        try
        {
            HttpServletResponse localHttpServletResponse = response;
            if (localHttpServletResponse != null)
            {
                localHttpServletResponse.setHeader("Pragma", "No-cache");
                localHttpServletResponse.setHeader("Cache-Control", "no-cache");
                localHttpServletResponse.setDateHeader("Expires", 0L);
                localHttpServletResponse.setContentType(contentType);
                localHttpServletResponse.getWriter().write(content);
            }
        }
        catch (IOException localIOException)
        {
            localIOException.printStackTrace();
        }
    }

}
