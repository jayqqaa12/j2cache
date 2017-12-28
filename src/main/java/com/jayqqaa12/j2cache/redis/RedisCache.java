package com.jayqqaa12.j2cache.redis;

import com.jayqqaa12.j2cache.Cache;
import com.jayqqaa12.j2cache.serializer.SerializationUtils;
import com.jayqqaa12.j2cache.util.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisCache implements Cache  {
    private final static Logger log = LoggerFactory.getLogger(RedisCache.class);




    protected byte[] getHashKeyBytes(Object key) throws IOException {

        return SerializationUtils.serialize(key);
    }


    /**
     * 直接用toString
     * @param key
     * @return
     * @throws IOException
     */
    protected byte[] getKeyBytes(Object key) throws IOException {
        return key.toString().getBytes();
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
        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            Set<byte[]> bytes = cache.hkeys(region.getBytes());
            bytes.forEach((b) -> {
                try {
                    keys.add(SerializationUtils.deserialize(b));
                } catch (IOException e) {
                    throw new CacheException(e);
                }
            });
        } catch (Exception e) {
            log.error("Error occured when get data from redis2 of", e);
        }
        return keys;
    }

    /**
     * Get an item from the of, nontransactionally
     * region不为空。查询hash
     * region为空 直接返回get(key)
     *
     * @param region of region
     * @param key    of key
     * @return the cached object or null
     */
    @Override
    public Object get(String region, Object key) throws CacheException {
        if (null == key)
            return null;
        if (region == null) {//直接获取值
            return get(key);
        }
        Object obj = null;
        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            byte[] b = cache.hget(region.getBytes(), getHashKeyBytes(key));
            if (b != null)
                obj = SerializationUtils.deserialize(b);
        } catch (Exception e) {
            log.error("Error occured when get data from redis2 of", e);
            if (e instanceof IOException || e instanceof NullPointerException)
                remove(region, key);
        }
        return obj;

    }


    public Object get(Object key) throws CacheException {
        Object obj = null;
        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            byte[] b = cache.get(getKeyBytes(key));
            if (b != null)
                obj = SerializationUtils.deserialize(b);
        } catch (Exception e) {
            log.error("Error occured when get data from redis2 of", e);
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
     * @param key     of key
     * @param value   of value
     * @param seconds of Expiration time
     * @throws CacheException
     */
    @Override
    public void set(String region, Object key, Object value, int seconds) throws CacheException {
        if (key == null)
            return;
        if (value == null)
            remove(region, key);
        else if (region == null) {
            set(key, seconds, value);

        } else if (region != null && seconds > 0) {
            set(appendHashNameSpace(region, key), seconds, value);
        } else {
            try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                cache.hset(region.getBytes(), getHashKeyBytes(key), SerializationUtils.serialize(value));
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
    private String appendHashNameSpace(String region, Object key) {
        return region + ":" + key.toString();
    }


    @Override
    public void batchSet(String region, Map<?, ?> data, int seconds) throws CacheException {
        if (data == null || data.isEmpty())
            return;
        else if (region == null) {
            bastchSet(data, seconds);
        } else {
            try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                Pipeline p = cache.pipelined();
                for (Object k : data.keySet()) {
                    if (k == null) continue;
                    Object v = data.get(k);
                    if (v == null) remove(region, k);
                    p.hset(region.getBytes(), getHashKeyBytes(k), SerializationUtils.serialize(v));
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

        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
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
    public void set(Object key, int seconds, Object value) {
        if (key == null)
            return;
        if (value == null)
            remove(key);
        else {
            try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                //为缓解缓存击穿 l2 缓存时间增加一点时间
                if (seconds > 0)
                    cache.setex(getKeyBytes(key), (int) (seconds * 1.1), SerializationUtils.serialize(value));
                else cache.set(getKeyBytes(key), SerializationUtils.serialize(value));
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }

    public void bastchSet(Map<?, ?> data, int seconds) {

        if (data == null || data.isEmpty()) return;
        else {
            try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                Pipeline p = cache.pipelined();
                for (Object k : data.keySet()) {
                    if (k == null) continue;
                    Object v = data.get(k);
                    if (v == null) remove(k);
                    //为缓解缓存击穿 l2 缓存时间增加一点时间
                    if (seconds > 0)
                        p.setex(getKeyBytes(k), (int) (seconds * 1.1), SerializationUtils.serialize(v));
                    else p.set(getKeyBytes(k), SerializationUtils.serialize(v));
                }
                p.sync();
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }


    /**
     * @param key Cache key
     *            Remove an item from the of
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
                try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                    int size = keys.size();
                    byte[][] okeys = new byte[size][];
                    for (int i = 0; i < size; i++) {
                        okeys[i] = getHashKeyBytes(keys.get(i));
                    }
                    cache.hdel(region.getBytes(), okeys);
                } catch (Exception e) {
                    throw new CacheException(e);
                }
            } else {
                try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                    cache.hdel(region.getBytes(), getHashKeyBytes(key));
                } catch (Exception e) {
                    throw new CacheException(e);
                }
            }

        }

    }


    /**
     * @param key Cache key
     *            Remove an item from the of
     */
    public void remove(Object key) throws CacheException {
        if (key == null)
            return;
        if (key instanceof List) {
            List keys = (List) key;
            try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                int size = keys.size();
                byte[][] okeys = new byte[size][];
                for (int i = 0; i < size; i++) {
                    okeys[i] = getKeyBytes(key);
                }
                cache.del(okeys);
            } catch (Exception e) {
                throw new CacheException(e);
            }
        } else {

            try (Jedis cache = RedisConnConfig.getPool().getResource()) {
                cache.del(getKeyBytes(key));
            } catch (Exception e) {
                throw new CacheException(e);
            }
        }
    }

    /**
     * Clear the of
     */
    @Override
    public void clear(String region) throws CacheException {
        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            cache.del(region.getBytes());
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /**
     * update exprie time
     *
     * @param key
     * @param seconds
     */

    public Object exprie(String region, Object key, int seconds) {
        if (key == null)
            return null;
        else if (region != null && !region.isEmpty()) {
            key = appendHashNameSpace(region, key);
        }

        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            return cache.expire(getKeyBytes(key), seconds);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }


    public void destory() {
        RedisConnConfig.getPool().destroy();
    }


}
