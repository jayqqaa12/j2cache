package com.jayqqaa12.j2cache.core;

import com.jayqqaa12.j2cache.ehcache.EhCacheProvider;
import com.jayqqaa12.j2cache.nullcache.NullCacheProvider;
import com.jayqqaa12.j2cache.redis.RedisCacheProvider;
import com.jayqqaa12.j2cache.util.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * L1,L2缓存管理器，调用缓存实现类
 */
public class CacheManager {

    private final static Logger log = LoggerFactory.getLogger(CacheManager.class);

    public static CacheProvider l1Provider;
    public static CacheProvider l2Provider;

    static {
        init();
    }

    protected static void init() {
        try {
            if (l1Provider != null && l2Provider != null) return;

            l1Provider = getProviderInstance(CacheConstans.L1_PROVIDER);
            l1Provider.start();
            log.info("Using L1 CacheProvider : " + l1Provider.getClass().getName());
            l2Provider = getProviderInstance(CacheConstans.L2_PROVIDER);
            l2Provider.start();
            log.info("Using L2 CacheProvider : " + l2Provider.getClass().getName());

        } catch (Exception e) {
            throw new CacheException("Unabled to initialize cache providers", e);
        }
    }

    private final static CacheProvider getProviderInstance(String value) throws Exception {
        if (CacheConstans.EHCACHE.equalsIgnoreCase(value)) {
            return new EhCacheProvider();
        } else if (CacheConstans.REDIS.equalsIgnoreCase(value)) {
            return new RedisCacheProvider();
        } else if ("none".equalsIgnoreCase(value)) {
            return new NullCacheProvider();
        }
        return (CacheProvider) Class.forName(value).newInstance();
    }


    private final static Cache getCache(int level, String cacheName) {
        return getCache(level,cacheName,true);
    }


    private final static Cache getCache(int level, String cacheName,boolean isCreate) {
        return ((level == 1) ? l1Provider : l2Provider).buildCache(cacheName,isCreate);
    }



    public final static void shutdown(int level) {
        ((level == 1) ? l1Provider : l2Provider).stop();
    }

    /**
     * 获取缓存中的数据
     *
     * @param level  Cache Level: L1 and L2
     * @param region Cache region name
     * @param key    Cache key
     * @return Cache object
     */
    public final static Object get(int level, String region, Serializable key) {
        if (key != null) {
            Cache cache = getCache(level, region);
            if (cache != null)
                return cache.get(region, key);
        }
        return null;
    }

    /**
     * 获取缓存中的数据
     *
     * @param level       Cache Level -&gt; L1 and L2
     * @param resultClass Cache object class
     * @param name        Cache region name
     * @param key         Cache key
     * @return Cache object
     */
    public final static <T> T get(int level, Class<T> resultClass, String name, Serializable key) {
        if (key != null) {
            Cache cache = getCache(level, name);
            if (cache != null)
                return (T) cache.get(name, key);
        }
        return null;
    }

    /**
     * 写入缓存
     *
     * @param level   Cache Level: L1 and L2
     * @param region  Cache region name
     * @param key     Cache key
     * @param value   Cache value
     * @param seconds Cache time
     */
    public final static void set(int level, String region, Serializable key, Object value, int seconds) {
        if (key != null && value != null) {
            Cache cache = getCache(level, region);
            if (cache != null)
                cache.set(region, key, value, seconds);
        }
    }

    /**
     * 清除缓存中的某个数据
     *
     * @param level  Cache Level: L1 and L2
     * @param region Cache region name
     * @param key    Cache key
     */
    public final static void remove(int level, String region, Serializable key) {
        if (key != null) {
            Cache cache = getCache(level, region,false);
            if (cache != null)
                cache.remove(region, key);
        }
    }

    /**
     * 批量删除缓存中的一些数据
     *
     * @param level  Cache Level： L1 and L2
     * @param region Cache region name
     * @param keys   Cache keys
     */
    public final static void batchRemove(int level, String region, List keys) {
        if (keys != null && keys.size() > 0) {
            Cache cache = getCache(level, region,false);
            if (cache != null)
                cache.remove(region, keys);
        }
    }


    public final static void batchSet(int level, String region, Map<Serializable,Object> data,int seconds) {
        if (data != null && !data.isEmpty()) {
            Cache cache = getCache(level, region);
            if (cache != null)
                cache.batchSet(region,data,seconds);
        }
    }


    public final static Object exprie(int level, String region, Serializable key, int seconds) {
        if (key != null) {
            Cache cache = getCache(level, region);
            if (cache != null) {
                cache.exprie(region, key, seconds);
                return cache.get(region, key);
            }

        }
        return null;
    }


    public final static void clear(int level, String name) throws CacheException {
        Cache cache = getCache(level, name,false);
        if (cache != null)
            cache.clear(name);
    }


    public static <T>  List<T>  keys(int level, String region) {
        Cache cache = getCache(level, region);
        if (cache != null)
            return cache.keys(region);
        return new ArrayList<>();
    }

    public static <T>  List<T>  batchGet(int level, String region) {
        Cache cache = getCache(level, region);
        if (cache != null)
            return cache.batchGet(region);
        return new ArrayList<>();
    }
}
