package com.jayqqaa12.j2cache.redis;

import com.jayqqaa12.j2cache.core.Cache;
import com.jayqqaa12.j2cache.core.CacheConstans;
import com.jayqqaa12.j2cache.serializer.SerializationUtils;
import com.jayqqaa12.j2cache.util.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisCache implements Cache {
    private final static Logger log = LoggerFactory.getLogger(RedisCache.class);
    private final RedisConnConfig redisConnConfig;

    protected RedisCache(RedisConnConfig redisConnConfig) {
        if (redisConnConfig == null) throw new CacheException("redisConnConfig is null ");
        redisConnConfig.init();
        this.redisConnConfig = redisConnConfig;
        log.info(">>>>> RedisCache init success");
    }

    /**
     * 在region里增加一个可选的层级,作为命名空间,使结构更加清晰
     * 同时满足小型应用,多个J2Cache共享一个redis database的场景
     *
     * @param name
     * @return
     */
    private String appendNameSpace(Object name) {
        String nameSpace = CacheConstans.NAMESPACE;

        if (name == null) return nameSpace;

        if (nameSpace != null && !nameSpace.isEmpty()) {
            name = nameSpace + ":" + name;
        }
        return name.toString();
    }

    protected byte[] getKeyNameBytes(Object key) throws IOException {

        return SerializationUtils.serialize(key);
    }


    /**
     * 获取hash 的所有keys
     *
     * @param region
     * @return
     */
    @Override
    public List<Object> keys(String region) {

        List<Object> keys = new ArrayList<>();
        if (region == null) return keys;
        region = appendNameSpace(region);
        try (Jedis cache = redisConnConfig.getPool().getResource()) {
            Set<byte[]> bytes = cache.hkeys(region.getBytes());
            bytes.forEach((b) -> {
                try {
                    keys.add(SerializationUtils.deserialize(b));
                } catch (IOException e) {
                    throw new CacheException(e);
                }
            });
        } catch (Exception e) {
            log.error("Error occured when get data from redis2 cache", e);
        }
        return keys;
    }

    /**
     * Get an item from the cache, nontransactionally
     * region不为空。查询hash
     * region为空 直接返回get(key)
     *
     * @param region cache region
     * @param key    cache key
     * @return the cached object or null
     */
    @Override
    public Object get(String region, Serializable key) throws CacheException {
        if (null == key)
            return null;
        if (region == null) {//直接获取值
            return get(key);
        }
        region = appendNameSpace(region);
        Object obj = null;
        try (Jedis cache = redisConnConfig.getPool().getResource()) {
            byte[] b = cache.hget(region.getBytes(), getKeyNameBytes(key));
            if (b != null)
                obj = SerializationUtils.deserialize(b);
        } catch (Exception e) {
            log.error("Error occured when get data from redis2 cache", e);
            if (e instanceof IOException || e instanceof NullPointerException)
                remove(region, key);
        }
        return obj;

    }


    public Object get(Serializable key) throws CacheException {
        String _key = appendNameSpace(key);
        Object obj = null;
        try (Jedis cache = redisConnConfig.getPool().getResource()) {
            byte[] b = cache.get(_key.getBytes());
            if (b != null)
                obj = SerializationUtils.deserialize(b);
        } catch (Exception e) {
            log.error("Error occured when get data from redis2 cache", e);
            if (e instanceof IOException || e instanceof NullPointerException)
                remove(key);
        }
        return obj;
    }


    /**
     * 因为hash 不能设置超时 所以
     * <p>
     * 有设置时间的都自动转为普通key
     *
     * @param region
     * @param key     cache key
     * @param value   cache value
     * @param seconds cache Expiration time
     * @throws CacheException
     */
    @Override
    public void set(String region, Serializable key, Object value, int seconds) throws CacheException {
        if (key == null)
            return;
        if (value == null)
            remove(region, key);
        else if (region == null) {
            set(key, seconds, value);

        } else if (region != null && seconds > 0) {
            set(appendHashNameSpace(region, key), seconds, value);
        } else {
            String _region = appendNameSpace(region);
            try (Jedis cache = redisConnConfig.getPool().getResource()) {
                cache.hset(_region.getBytes(), getKeyNameBytes(key), SerializationUtils.serialize(value));
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }


    /**
     * 如果是
     *
     * @param key
     * @return
     */
    private String appendHashNameSpace(String region, Serializable key) {
        return region + ":" + key.toString();
    }


    public void batchSet(String region, Map<Serializable, Object> data, int seconds) throws CacheException {
        if (data == null || data.isEmpty())
            return;
        else if (region == null) {
            bastchSet(data, seconds);
        } else {
            String _region = appendNameSpace(region);
            try (Jedis cache = redisConnConfig.getPool().getResource()) {
                Pipeline p = cache.pipelined();
                for (Serializable k : data.keySet()) {
                    if (k == null) continue;
                    Object v = data.get(k);
                    if (v == null) remove(region, k);
                    p.hset(_region.getBytes(), getKeyNameBytes(k), SerializationUtils.serialize(v));
                }

                p.sync();
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }

    @Override
    public <T> List<T> batchGet(String region) throws CacheException {

        List<T> list = new ArrayList();
        if (region == null) return list;

        region = appendNameSpace(region);
        try (Jedis cache = redisConnConfig.getPool().getResource()) {
            cache.hgetAll(region).forEach((k, v) -> {
                Object obj = null;
                try {
                    obj = SerializationUtils.deserialize(v.getBytes());
                } catch (IOException e) {
                    throw new CacheException(e);
                }
                if (obj != null) list.add((T) obj);
            });
        } catch (Exception e) {
            throw new CacheException(e);
        }

        return list;
    }


    /**
     * 设置超时时间
     *
     * @param key     键
     * @param seconds 时间（秒） 60*60为一小时
     * @param value   值
     * @return
     */
    public void set(Serializable key, int seconds, Object value) {
        if (key == null)
            return;
        if (value == null)
            remove(key);
        else {
            String _key = appendNameSpace(key);
            try (Jedis cache = redisConnConfig.getPool().getResource()) {
                //为缓解缓存击穿 l2 缓存时间增加一点时间
                if (seconds > 0)
                    cache.setex(_key.getBytes(), (int) (seconds * 1.5), SerializationUtils.serialize(value));
                else cache.set(_key.getBytes(), SerializationUtils.serialize(value));
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }

    public void bastchSet(Map<Serializable, Object> data, int seconds) {

        if (data == null || data.isEmpty()) return;
        else {
            try (Jedis cache = redisConnConfig.getPool().getResource()) {
                Pipeline p = cache.pipelined();
                for (Serializable k : data.keySet()) {
                    if (k == null) continue;
                    String _key = appendNameSpace(k);
                    Object v = data.get(k);
                    if (v == null) remove(k);
                    //为缓解缓存击穿 l2 缓存时间增加一点时间
                    if (seconds > 0)
                        p.setex(_key.getBytes(), (int) (seconds * 1.5), SerializationUtils.serialize(v));
                    else p.set(_key.getBytes(), SerializationUtils.serialize(v));
                }
                p.sync();
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }


    /**
     * @param key Cache key
     *            Remove an item from the cache
     */
    @Override
    public void remove(String region, Object key) throws CacheException {
        if (key == null)
            return;
        if (region == null) {
            remove(key);
        } else {
            if (key instanceof List) {
                List keys = (List) key;
                try (Jedis cache = redisConnConfig.getPool().getResource()) {
                    int size = keys.size();
                    byte[][] okeys = new byte[size][];
                    for (int i = 0; i < size; i++) {
                        okeys[i] = getKeyNameBytes(keys.get(i));
                    }
                    String _region = appendNameSpace(region);
                    cache.hdel(_region.getBytes(), okeys);
                } catch (Exception e) {
                    throw new CacheException(e);
                }
            } else {
                region = appendNameSpace(region);
                try (Jedis cache = redisConnConfig.getPool().getResource()) {
                    cache.hdel(region.getBytes(), getKeyNameBytes(key));
                } catch (Exception e) {
                    throw new CacheException(e);
                }
            }

        }

    }


    /**
     * @param key Cache key
     *            Remove an item from the cache
     */
    public void remove(Object key) throws CacheException {
        if (key == null)
            return;
        if (key instanceof List) {
            List keys = (List) key;
            try (Jedis cache = redisConnConfig.getPool().getResource()) {
                int size = keys.size();
                byte[][] okeys = new byte[size][];
                for (int i = 0; i < size; i++) {
                    String _key = appendNameSpace(keys.get(i));
                    okeys[i] = getKeyNameBytes(_key);
                }
                cache.del(okeys);
            } catch (Exception e) {
                throw new CacheException(e);
            }
        } else {

            String _key = appendNameSpace(key);
            try (Jedis cache = redisConnConfig.getPool().getResource()) {
                cache.del(_key.getBytes());
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }

    /**
     * Clear the cache
     */
    @Override
    public void clear(String region) throws CacheException {
        try (Jedis cache = redisConnConfig.getPool().getResource()) {
            region = appendNameSpace(region);
            cache.del(getKeyNameBytes(region));
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /**
     * update exprie time
     * <p>
     * //FIXME
     * hash 不能更新 所以直接返回
     *
     * @param key
     * @param seconds
     */

    public Object exprie(String region, Serializable key, int seconds) {
        if (key == null)
            return null;
        if (region == null || region.isEmpty()) {
            String _key = appendNameSpace(key);
            try (Jedis cache = redisConnConfig.getPool().getResource()) {
                return cache.expire(getKeyNameBytes(_key), seconds);
            } catch (Exception e) {
                throw new CacheException(e);
            }
        } else {
            return get(region, key);
        }
    }


    public void destory() {
        redisConnConfig.getPool().destroy();
    }
}
