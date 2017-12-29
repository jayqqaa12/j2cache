package com.jayqqaa12.j2cache.redis.lock;

import com.jayqqaa12.j2cache.redis.RedisClient;
import com.jayqqaa12.j2cache.serializer.SerializationUtils;

/**
 * Created by 12 on 2017/7/17.
 */
public class JedisLock extends AbstractRedisLock {

    private RedisClient jedis;

    public JedisLock(RedisClient jedis) {
        this.jedis = jedis;
    }

    @Override
    protected Long setnx(String key, String val) {
        return this.jedis.get().setnx(key.getBytes(), val.getBytes());
    }

    @Override
    protected void expire(String key, int expire) {
        this.jedis.get().expire(key.getBytes(), expire);
    }

    @Override
    protected String get(String key) {
        return SerializationUtils.deserialize(this.jedis.get().get(key.getBytes())).toString();
    }

    @Override
    protected String getSet(String key, String newVal) {
        return SerializationUtils.deserialize(jedis.get().getSet(key.getBytes(), newVal.getBytes())).toString();
    }

    @Override
    protected void del(String key) {
        jedis.get().del(key.getBytes());
    }

}
