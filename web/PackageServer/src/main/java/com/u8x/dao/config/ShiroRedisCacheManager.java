package com.u8x.dao.config;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 因为我们已经集成了Spring Redis， Shiro中，我们自定义一个CacheManager，使用Spring Cache
 * Created by ant on 2017/5/29.
 */
public class ShiroRedisCacheManager implements CacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {

        return new ShiroCache<K,V>(name, redisTemplate);
    }



    public class ShiroCache<K, V> implements Cache<K, V> {

        private static final String REDIS_SHIRO_CACHE = "xpgs-shiro-cache:";
        private String cacheKey;
        private RedisTemplate<K, V> redisTemplate;
        private long globExpire = 30;           //密码默认锁定30分钟

        @SuppressWarnings("rawtypes")
        public ShiroCache(String name, RedisTemplate client) {
            this.cacheKey = REDIS_SHIRO_CACHE + name + ":";
            this.redisTemplate = client;
        }

        @Override
        public V get(K key) throws CacheException {
            redisTemplate.boundValueOps(getCacheKey(key)).expire(globExpire, TimeUnit.MINUTES);
            return redisTemplate.boundValueOps(getCacheKey(key)).get();
        }

        @Override
        public V put(K key, V value) throws CacheException {
            V old = get(key);
            redisTemplate.boundValueOps(getCacheKey(key)).set(value);
            return old;
        }

        @Override
        public V remove(K key) throws CacheException {
            V old = get(key);
            redisTemplate.delete(getCacheKey(key));
            return old;
        }

        @Override
        public void clear() throws CacheException {
            redisTemplate.delete(keys());
        }

        @Override
        public int size() {
            return keys().size();
        }

        @Override
        public Set<K> keys() {
            return redisTemplate.keys(getCacheKey("*"));
        }

        @Override
        public Collection<V> values() {
            Set<K> set = keys();
            List<V> list = new ArrayList<>();
            for (K s : set) {
                list.add(get(s));
            }
            return list;
        }

        private K getCacheKey(Object k) {
            return (K) (this.cacheKey + k);
        }
    }
}
