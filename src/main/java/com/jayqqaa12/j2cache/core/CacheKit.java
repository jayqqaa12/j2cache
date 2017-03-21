package com.jayqqaa12.j2cache.core;

import com.jayqqaa12.j2cache.redis.RedisConnConfig;
import com.jayqqaa12.j2cache.util.CacheException;
import com.jayqqaa12.j2cache.util.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

import java.util.ArrayList;
import java.util.List;


public class CacheKit {
    private final static Logger log = LoggerFactory.getLogger(CacheKit.class);
    private static CacheKit cache = new CacheKit();

    public final static byte LEVEL1 = 1;
    public final static byte LEVEL2 = 2;

    public static CacheKit cache() {
        return cache;
    }
    private CacheKit() {
        CacheManager.init();
    }

    /**
     * 从缓存中取数据,先从1级取，再从2级取
     * region 为空,key的格式
     * EHCACHE:从默认的region中获取
     * memcache:NAMESPACE:key
     * REDIS:NAMESPACE:key
     *
     * @param key cache key
     * @return the cached object or null
     */
    public Object get(String key) {
        return get(null, key);
    }


    /**
     * 从缓存中取数据,先从1级取，再从2级取
     *
     * @param region cache region
     *               key格式：
     *               EHCACHE：用region,key
     *               memcache：NAMESPACE:region:key
     *               REDIS：使用hashs(region,key)
     * @param key    cache key
     * @return the
     * cached object or null
     */
    public Object get(String region, String key) {
        Object obj = null;
        if (key != null) {
            obj = CacheManager.get(LEVEL1, region, key);
            if (obj == null) {
                obj = CacheManager.get(LEVEL2, region, key);
                if (obj != null) {
                    CacheManager.set(LEVEL1, region, key, obj, CacheConstans.DEFAULT_TIME);
                }
            }
        }
        return obj;
    }


    public  List<Object> keys(int level ,String region){

        List<Object> keys =new ArrayList<>();
       if(region!=null){
         keys=   CacheManager.keys(level,region);
       }
       return  keys;

    }


    /**
     * 获取数据,指定缓存的级别level
     *
     * @param level  cache level
     * @param region cache region
     * @param key    cache key
     * @return the cached object or null
     */
    public Object get(int level, String region, String key) {
        Object obj = null;
        if (key != null) {
            obj = CacheManager.get(level, region, key);
        }
        return obj;
    }

    /**
     * 获取取数据,指定缓存的级别level
     *
     * @param level cache level
     * @param key   cache key
     * @return the cached object or null
     */
    public Object get(int level, String key) {
        return get(level, CacheConstans.NUllRegion, key);
    }

    /**
     * Add an item to the cache
     *
     * @param region
     * @param key     cache key
     * @param value   cache value
     * @param seconds cache Expiration time
     */
    public void set(String region, String key, Object value, int seconds,boolean notify) {
        if (key != null) {
            if (value == null)
                remove(region, key);
            else {
                if(notify) sendEvictCmd(region, key);// 清除原有的一级缓存的内容
                CacheManager.set(LEVEL1, region, key, value, seconds);
                CacheManager.set(LEVEL2, region, key, value, seconds);
            }
        }
    }

    /**
     * 写入缓存
     *
     * @param region : Cache Region name
     * @param key    : Cache key
     * @param value  : Cache value
     */
    public void set(String region, String key, Object value,boolean notify) {
        set(region, key, value, CacheConstans.DEFAULT_TIME,notify);
    }

    /**
     * Add an item to the cache
     * region use default value
     *
     * @param key   cache key
     * @param value cache value
     *              seconds cache Expiration use default time
     */
    public void set(String key, Object value,boolean notify) {
        set(CacheConstans.NUllRegion, key, value, CacheConstans.DEFAULT_TIME,notify);
    }

    /**
     * Add an item to the cache
     * region use default value
     *
     * @param key   cache key
     * @param value cache value
     *              seconds cache Expiration use default time
     */
    public void set(String key, Object value, int seconds,boolean notify) {
        set(CacheConstans.NUllRegion, key, value, seconds,notify);
    }

    /**
     * Add an item to the cache
     *
     * @param level   cache level
     * @param region
     * @param key     cache key
     * @param value   cache value
     * @param seconds cache Expiration time
     */
    public void set(int level, String region, String key, Object value, int seconds,boolean notify) {
        if (key != null) {
            if (value == null)
                remove(region, key);
            else {
               if(notify) sendEvictCmd(region, key);// 清除原有的一级缓存的内容
                CacheManager.set(level, region, key, value, seconds);
            }
        }
    }

    /**
     * Add an item to the cache
     *
     * @param level  cache level
     * @param region
     * @param key    cache key
     * @param value  cache value
     *               seconds cache Expiration use default time
     */
    public void set(int level, String region, String key, Object value,boolean notify) {
        set(level, region, key, value, CacheConstans.DEFAULT_TIME,notify);
    }

    /**
     * Add an item to the cache
     *
     * @param level cache level
     * @param key   cache key
     * @param value cache value
     *              seconds cache Expiration use default time
     */
    public void set(int level, String key, Object value,boolean notify) {
        set(level, CacheConstans.NUllRegion, key, value, CacheConstans.DEFAULT_TIME,notify);
    }

    /**
     * @param key Cache key
     *            Remove an item from the cache
     */
    public void remove(String key) {
        sendEvictCmd(CacheConstans.NUllRegion, key);
        CacheManager.remove(LEVEL1, CacheConstans.NUllRegion, key);
        CacheManager.remove(LEVEL2, CacheConstans.NUllRegion, key);
    }

    /**
     * 批量删除缓存
     *
     * @param region : Cache region name
     * @param keys   : Cache key
     */
    public void remove(String region, List<String > keys) {
        for (String key : keys) {
            sendEvictCmd(region, key);
        }
        CacheManager.batchRemove(LEVEL1,region, keys);
        CacheManager.batchRemove(LEVEL2,region, keys);
    }

    public void remove(List<String> keys) {
        for (String key : keys) {
            sendEvictCmd(CacheConstans.NUllRegion, key);
        }
        CacheManager.batchRemove(LEVEL1, CacheConstans.NUllRegion, keys);
        CacheManager.batchRemove(LEVEL2, CacheConstans.NUllRegion, keys);
    }

    /**
     * 删除缓存
     *
     * @param region : Cache Region name
     * @param key    : Cache key
     */
    public void remove(String region, String key) {
        CacheManager.remove(LEVEL1, region, key); // 删除一级缓存
        CacheManager.remove(LEVEL2, region, key); // 删除二级缓存
        sendEvictCmd(region, key); // 发送广播
    }


    /**
     * Clear the cache
     *
     * @param region : Cache region name
     */
    public void clear(String region) throws CacheException {
        CacheManager.clear(LEVEL1, region);
        CacheManager.clear(LEVEL2, region);
        sendEvictCmd(region, null);
    }

    /**
     * 关闭到通道的连接
     */
    public void close() {
        CacheManager.shutdown(LEVEL1);
        CacheManager.shutdown(LEVEL2);
    }

    /**
     * update exprie time
     *
     * @param key
     * @param seconds
     */
    public Object exprie(String key, int seconds) {
        return exprie(CacheConstans.NUllRegion, key, seconds);
    }

    /**
     * update exprie time
     *
     * @param key
     * @param seconds
     */
    public Object exprie(String region, String key, int seconds) {
        CacheManager.exprie(LEVEL1, region, key, seconds);
        CacheManager.exprie(LEVEL2, region, key, seconds);
        return get(region, key);
    }


    /**
     * 发送清除缓存的广播命令，通知其他的1级缓存也删除
     *
     * @param region : Cache region name
     * @param key    : cache key
     */
    public void sendEvictCmd(String region, String key) {

        Command cmd = new Command(Command.OPT_DELETE_KEY, region, key);
        try (Jedis jedis = RedisConnConfig.getPool().getResource()) {
            jedis.publish(SafeEncoder.encode(CacheConstans.REDIS_CHANNEL), cmd.toBuffers());
        } catch (Exception e) {
            log.error("Unable to delete cache,region=" + region + ",key=" + key, e);
        }

    }
}
