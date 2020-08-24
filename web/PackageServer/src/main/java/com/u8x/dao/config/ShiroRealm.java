package com.u8x.dao.config;

import com.u8x.common.Consts;
import com.u8x.dao.AdminDao;
import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.ShiroToken;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 权限处理类
 * Created by ant on 2017/5/29.
 */
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    @Lazy
    private AdminDao adminDao;

    //验证当前登录管理员是否有对应功能的权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("doGetAuthorizationInfo called");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        return info;
    }

    //该方法中，验证登录是否有效
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken tk) throws AuthenticationException {
        ShiroToken token = (ShiroToken)tk;
        if(token != null){
            Admin admin = adminDao.getAdminByName(token.getPrincipal());
            if(admin == null){
                throw new AccountException(Consts.Tips.UserNameError);
            }
            return new SimpleAuthenticationInfo(admin, admin.getPassword(), admin.getUsername());
        }
        return null;
    }
}
