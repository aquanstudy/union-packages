package com.u8x.dao.config;

import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.dao.AdminDao;
import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.ShiroToken;
import com.u8x.utils.FileUtils;
import com.u8x.utils.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限制管理员密码尝试次数
 */
public class RetryLimitCredentialsMatcher extends SimpleCredentialsMatcher{
    private static final XLogger logger = XLogger.getLogger(RetryLimitCredentialsMatcher.class);

    /**
     * 最大重试次数
     */
    private static final int MAX_TRY_NUM = 3;

    private Cache<String, AtomicInteger> passwordRetryCache;


    public RetryLimitCredentialsMatcher(CacheManager cacheManager){
        passwordRetryCache = cacheManager.getCache("prc");
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {

        //获取用户名
        String username = (String)token.getPrincipal();
        //获取用户登录次数
        AtomicInteger retryCount = passwordRetryCache.get(username);
        if (retryCount == null) {
            //如果用户没有登陆过,登陆次数加1 并放入缓存
            retryCount = new AtomicInteger(0);
            passwordRetryCache.put(username, retryCount);
        }
        if (retryCount.incrementAndGet() > MAX_TRY_NUM) {
            logger.error("admin {} has been locked. ", username);
            throw new LockedAccountException(Consts.Tips.ALOCK);
        }else{
            passwordRetryCache.put(username, retryCount);
        }
        //判断用户账号和密码是否正确
        Admin admin = (Admin) info.getPrincipals().getPrimaryPrincipal();

        if(!admin.getPassword().equals(token.getCredentials())){
            throw new AccountException(Consts.Tips.PasswordError);
        }

        passwordRetryCache.remove(username);

        return true;
    }

    /**
     * 根据用户名 解锁用户
     * @param username
     * @return
     */
    public void unlockAccount(String username){
        if (StringUtils.isNotEmpty(username)){
            //修改数据库的状态字段为锁定
            passwordRetryCache.remove(username);
        }
    }


}
