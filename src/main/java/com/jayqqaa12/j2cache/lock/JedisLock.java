package com.jayqqaa12.j2cache.lock;

import redis.clients.jedis.Jedis;
/**
 * Created by 12 on 2017/7/17.
 */
public class JedisLock extends AbstractRedisLock {

    private Jedis jedis;

    public JedisLock(Jedis jedis) {
        this.jedis =jedis;
    }

    @Override
    protected Long setnx(String key, String val) {
        return this.jedis.setnx(key, val);
    }

    @Override
    protected void expire(String key, int expire) {
        this.jedis.expire(key, expire);
    }

    @Override
    protected String get(String key) {
        return this.jedis.get(key);
    }

    @Override
    protected String getSet(String key, String newVal) {
        return this.jedis.getSet(key, newVal);
    }

    @Override
    protected void del(String key) {
        this.jedis.del(key);
    }

}
