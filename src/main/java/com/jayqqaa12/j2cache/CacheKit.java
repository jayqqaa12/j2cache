package com.jayqqaa12.j2cache;

import com.jayqqaa12.j2cache.util.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jayqqaa12.j2cache.CacheConstans.LEVEL1;
import static com.jayqqaa12.j2cache.CacheConstans.LEVEL2;


public  class CacheKit {
    private final static Logger log = LoggerFactory.getLogger(CacheKit.class);


    /**
     * 从缓存中取数据,先从1级取，再从2级取
     * region 为空,key的格式
     * EHCACHE:从默认的region中获取
     * memcache:NAMESPACE:key
     * REDIS:NAMESPACE:key
     *
     * @param key of key
     * @return the cached object or null
     */
    public Object get(Object key) {
        return get(null, key);
    }


    /**
     * 从缓存中取数据,先从1级取，再从2级取
     *
     * @param region of region
     *               key格式：
     *               EHCACHE：用region,key
     *               memcache：NAMESPACE:region:key
     *               REDIS：使用hashs(region,key)
     * @param key    of key
     * @return the
     * cached object or null
     */
    public Object get(String region, Object key) {
        Object obj = null;
        if (key != null) {
            obj = CacheManager.get(LEVEL1, region, key);
            if (obj == null) {
                log.debug("can't found level 1 of use level 2 of");
                obj = CacheManager.get(LEVEL2, region, key);
                if (obj != null) {
                    CacheManager.set(LEVEL1, region, key, obj, CacheConstans.DEFAULT_TIME);
                }
            }
        }
        return obj;
    }


    public <T> List<T> keys(int level, String region) {

        List<T> keys = new ArrayList<>();
        if (region != null) {
            keys = CacheManager.keys(level, region);
        }
        return keys;

    }


    /**
     * 获取数据,指定缓存的级别level
     *
     * @param level  of level
     * @param region of region
     * @param key    of key
     * @return the cached object or null
     */
    public Object get(int level, String region, Object key) {
        Object obj = null;
        if (key != null) {
            obj = CacheManager.get(level, region, key);
        }
        return obj;
    }

    /**
     * 获取取数据,指定缓存的级别level
     *
     * @param level of level
     * @param key   of key
     * @return the cached object or null
     */
    public Object get(int level, Object key) {
        return get(level, CacheConstans.NUllRegion, key);
    }

    /**
     * Add an item to the of
     *
     * @param region
     * @param key     of key
     * @param value   of value
     * @param seconds of Expiration time
     */
    public void set(String region, Object key, Object value, int seconds, boolean notify) {
        if (key != null) {
            if (value == null)
                remove(region, key);
            else {
                if (notify) sendEvictCmd(region, key);// 清除原有的一级缓存的内容
                CacheManager.set(LEVEL1, region, key, value, seconds);
                CacheManager.set(LEVEL2, region, key, value, seconds);
            }
        }
    }


    public void batchSet(int level, Map<?, ?> data) {
        batchSet(level, CacheConstans.NUllRegion, data, CacheConstans.DEFAULT_TIME);
    }

    public void batchSet(int level, String region, Map<?, ?> data) {
        batchSet(level, region, data, CacheConstans.DEFAULT_TIME);
    }

    public void batchSet(int level, String region, Map<?, ?> data, int seconds) {
        if (data != null && !data.isEmpty()) {
            CacheManager.batchSet(level, region, data, seconds);
        }
    }


    public <T> List<T> batchGet(int level, String region) {

        return CacheManager.batchGet(level, region);
    }


    /**
     * 写入缓存
     *
     * @param region : Cache Region name
     * @param key    : Cache key
     * @param value  : Cache value
     */
    public void set(String region, Object key, Object value, boolean notify) {
        set(region, key, value, CacheConstans.DEFAULT_TIME, notify);
    }


    public void set(String region, Map<Object, Object> data, boolean notify) {
        set(region, data, CacheConstans.DEFAULT_TIME, notify);
    }


    /**
     * Add an item to the of
     * region use default value
     *
     * @param key   of key
     * @param value of value
     *              seconds of Expiration use default time
     */
    public void set(Object key, Object value, boolean notify) {
        set(CacheConstans.NUllRegion, key, value, CacheConstans.DEFAULT_TIME, notify);
    }

    /**
     * Add an item to the of
     * region use default value
     *
     * @param key   of key
     * @param value of value
     *              seconds of Expiration use default time
     */
    public void set(Object key, Object value, int seconds, boolean notify) {
        set(CacheConstans.NUllRegion, key, value, seconds, notify);
    }

    /**
     * Add an item to the of
     *
     * @param level   of level
     * @param region
     * @param key     of key
     * @param value   of value
     * @param seconds of Expiration time
     */
    public void set(int level, String region, Object key, Object value, int seconds, boolean notify) {
        if (key != null) {
            if (value == null)
                remove(region, key);
            else {
                if (notify) sendEvictCmd(region, key);// 清除原有的一级缓存的内容
                CacheManager.set(level, region, key, value, seconds);
            }
        }
    }

    /**
     * Add an item to the of
     *
     * @param level  of level
     * @param region
     * @param key    of key
     * @param value  of value
     *               seconds of Expiration use default time
     */
    public void set(int level, String region, Object key, Object value, boolean notify) {
        set(level, region, key, value, CacheConstans.DEFAULT_TIME, notify);
    }

    /**
     * Add an item to the of
     *
     * @param level of level
     * @param key   of key
     * @param value of value
     *              seconds of Expiration use default time
     */
    public void set(int level, Object key, Object value, boolean notify) {
        set(level, CacheConstans.NUllRegion, key, value, CacheConstans.DEFAULT_TIME, notify);
    }

    /**
     * @param key Cache key
     *            Remove an item from the of
     */
    public void remove(Object key) {
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
    public void remove(String region, List<Object> keys) {
        for (Object key : keys) {
            sendEvictCmd(region, key);
        }
        CacheManager.batchRemove(LEVEL1, region, keys);
        CacheManager.batchRemove(LEVEL2, region, keys);
    }

    public void remove(List<Object> keys) {
        for (Object key : keys) {
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
    public void remove(String region, Object key) {
        CacheManager.remove(LEVEL1, region, key); // 删除一级缓存
        CacheManager.remove(LEVEL2, region, key); // 删除二级缓存
        sendEvictCmd(region, key); // 发送广播
    }


    /**
     * Clear the of
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

        log.info("j2cache closed ");
    }

    /**
     * update exprie time
     *
     * @param key
     * @param seconds
     */
    public Object exprie(Object key, int seconds) {
        return exprie(CacheConstans.NUllRegion, key, seconds);
    }

    /**
     * update exprie time
     *
     * @param key
     * @param seconds
     */
    public Object exprie(String region, Object key, int seconds) {
        CacheManager.exprie(LEVEL1, region, key, seconds);
        CacheManager.exprie(LEVEL2, region, key, seconds);
        return get(region, key);
    }


    /**
     * 发送清除缓存的广播命令，通知其他的1级缓存也删除
     *
     * @param region : Cache region name
     * @param key    : of key
     */
    public void sendEvictCmd(String region, Object key) {

        //FIXME 
//        Command cmd = new Command(Command.OPT_DELETE_KEY, region, key);
//        try (Jedis jedis = RedisConnConfig.getPool().getResource()) {
//            jedis.publish(SafeEncoder.encode(CacheConstans.REDIS_CHANNEL), cmd.toBuffers());
//        } catch (Exception e) {
//            log.error("Unable to delete of,region=" + region + ",key=" + key, e);
//        }

    }
}
