package com.jayqqaa12.j2cache.core;

import com.jayqqaa12.j2cache.util.CacheException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.jayqqaa12.j2cache.core.CacheKit.LEVEL1;
import static com.jayqqaa12.j2cache.core.CacheKit.LEVEL2;

/**
 * 缓存使用入口
 * <p>
 * set  设置数据同时放入一级和二级并通知其他节点
 * <p>
 * set1 只设置一级缓存
 * <p>
 * set2 只设置二级缓存
 * <p>
 * setn 设置数据但是 不发送通知
 *
 *
 */
public class J2Cache {


    public static CacheKit cache() {
        return CacheKit.cache();
    }

    /**
     * 获取数据
     *
     * @param key
     * @param data
     * @return
     * @throws CacheException
     */
    public static <T>T get(Serializable key, CacheDataSource data) throws CacheException {
        return get(CacheConstans.NUllRegion, key, data, CacheConstans.DEFAULT_TIME);
    }


    public static <T>T get(String region, Serializable key, CacheDataSource data) throws CacheException {
        return get(region, key, data, CacheConstans.DEFAULT_TIME);
    }


    public static <T>T get1(String region, Serializable key, CacheDataSource data, int sec) throws CacheException {

        Object obj = get1(region, key);
        if (obj == null) {
            obj = data.load();
            set1n(region, key, obj, sec);
        }
        return (T) obj;
    }

    public static <T>T get(Serializable key, CacheDataSource data, int sec) throws CacheException {
        return get(CacheConstans.NUllRegion, key, data, sec);
    }

    /**
     * 获取数据
     * 使用cache
     * 没有的话就调用获取数据接口来获取
     *
     * @param region
     * @param key
     * @param data
     * @return
     * @throws CacheException
     */
    private static <T>T get(String region, Serializable key, CacheDataSource data, int sec) throws CacheException {

        Object obj = cache().get(region, key);
        if (obj == null) {
            obj = data.load();
            cache().set(region,key, obj, sec, false);
        }
        return (T) obj;
    }

    public static void set(Serializable key, Object value, int seconds) {
        cache().set(CacheConstans.NUllRegion, key, value, seconds, true);
    }

    /**
     *
     * set 缓存的数据
     * 使用region redis 默认使用hash
     * ehcache 使用指定的region缓存
     * 注意相同的 region只能设置一次 超时时间 ！(ehcache)
     *
     * redis默认存的是hash 所以不能修改时间
     *
     *
     * @param key
     * @param value
     */
//    public static void set(String region, Serializable key, Object value, int seconds) {
//        cache().set(region, key, value, seconds,true);
//    }

    /**
     * set 1级缓存的数据
     * 使用region redis 默认使用hash
     * ehcache 使用指定的region缓存
     * 注意相同的 region只能设置一次 超时时间 ！(ehcache)
     *但是不发送给其他节点来清空缓存
     * @param key
     * @param value
     */
//    public static void setn(String region, Serializable key, Object value, int seconds) {
//        cache().set(region, key, value, seconds,false);
//    }

    /**
     * set 1级缓存的数据
     * 使用region redis 默认使用hash
     * ehcache 使用指定的region缓存
     * 注意相同的 region只能设置一次 超时时间 ！(ehcache)
     *
     * @param region
     * @param key
     * @param value
     * @param seconds
     */
    public static void set1(String region, Serializable key, Object value, int seconds) {
        cache().set(LEVEL1, region, key, value, seconds, true);
    }

    public static void set1n(String region, Serializable key, Object value, int seconds) {
        cache().set(LEVEL1, region, key, value, seconds, false);
    }


    /**
     * 获取数据
     * 使用region
     *
     * @param region
     * @param key
     * @return
     */
    public static <T>T get(String region, Serializable key) {
        return (T) cache().get(region, key);
    }


    public static <T>T get(Serializable key) {
        return (T) cache().get(key);
    }

    public static <T>T get1(String region, Serializable key) {
        return (T) cache().get(LEVEL1, region, key);
    }


    public static <T>T get1(Serializable key) {
        return (T) cache().get(LEVEL1, key);
    }

    public static <T>T get2(String region, Serializable key) {
        return (T) cache().get(LEVEL2, region, key);
    }

    public static <T>T get2(Serializable key) {
        return (T) cache().get(LEVEL2, key);
    }


    public static void set(Serializable key, Object value) {
        cache().set(key, value, true);
    }


    public static void set(String region, Serializable key, Object value) {
        cache().set(region, key, value, true);
    }


    public static void batchSet( Map< Serializable, Object> data) {
        cache().batchSet(data);
    }

    public static void batchSet(String region, Map< Serializable, Object> data) {
        cache().batchSet(region,data);
    }

    public static void batchSet(String region, Map<Serializable, Object> data,int seconds ) {
        cache().batchSet(region,data,seconds);
    }

    public static void setn(Serializable key, Object value) {
        cache().set(key, value, false);
    }


    public static void setn(String region, Serializable key, Object value) {
        cache().set(region, key, value, false);
    }


    public static void set1(Serializable key, Object value) {
        cache().set(key, value, true);
    }

    public static void set1n(Serializable key, Object value) {

        cache().set(key, value, false);
    }

    /**
     * set 1级缓存的数据
     * 使用region redis 默认使用hash
     * ehcache 使用指定的region缓存
     * 注意相同的 region只能设置一次 超时时间 ！(ehcache)
     *
     * @param region
     * @param key
     * @param value
     */
    public static void set1(String region, Serializable key, Object value) {
        cache().set(LEVEL1, region, key, value, true);
    }

    public static void set1n(String region, Serializable key, Object value) {
        cache().set(LEVEL1, region, key, value, false);
    }


    /**
     * set 2级缓存的数据
     * 使用region redis 默认使用hash
     * ehcache 使用指定的region缓存
     * 注意相同的 region只能设置一次 超时时间 ！(ehcache)
     *
     * @param region
     * @param key
     * @param value
     */
    public static void set2(String region, Serializable key, Object value) {
        cache().set(LEVEL2, region, key, value, true);
    }


    /**
     * set 2级缓存的数据
     * ehcache 使用指定的region缓存
     * 注意相同的 region只能设置一次 超时时间 ！(ehcache)
     *
     * @param key
     * @param value
     */
    public static void set2(Serializable key, Object value, int seconds) {
        cache().set(LEVEL2, CacheConstans.NUllRegion, key, value, seconds, true);
    }


    public static List<Object> keys1(String region) {
        return cache().keys(LEVEL1, region);
    }

    public static List<Object> keys2(String region) {
        return cache().keys(LEVEL2, region);
    }

    public static void remove(String region, Serializable key) {
        cache().remove(region, key);
    }

    public static void remove(Serializable key) {
        cache().remove(key);
    }

    public static void remove(String region, List<Serializable> keys) {
        cache().remove(region, keys);
    }

    public static void remove(List<Serializable> keys) {
        cache().remove(keys);
    }


    /**
     * 清空指定region所有缓存
     *
     * @param region
     */
    public static void clear(String region) {
        cache().clear(region);
    }


    /**
     * 释放缓存资源 关闭jvm的时候使用！！
     */
    public static void close() {
        cache().close();
    }
}
