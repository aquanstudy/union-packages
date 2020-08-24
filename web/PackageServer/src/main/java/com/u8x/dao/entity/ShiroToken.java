package com.u8x.dao.entity;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;

/**
 * 用户登陆token
 * Created by ant on 2017/5/30.
 */
public class ShiroToken implements AuthenticationToken, RememberMeAuthenticationToken {

    private String username;        //登录用户名
    private String password;        //登录密码
    private boolean rememberMe;     //记住帐号

    public ShiroToken(String username, String password, boolean rememberMe){
        super();
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    @Override
    public boolean isRememberMe() {
        return rememberMe;
    }

    @Override
    public String getPrincipal() {
        return this.username;
    }

    @Override
    public String getCredentials() {
        return this.password;
    }
}
