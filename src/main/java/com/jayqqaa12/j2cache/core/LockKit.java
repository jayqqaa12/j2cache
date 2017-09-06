package com.jayqqaa12.j2cache.core;

import com.jayqqaa12.j2cache.lock.JedisLock;
import com.jayqqaa12.j2cache.lock.JedisRateLimiter;
import com.jayqqaa12.j2cache.redis.RedisConnConfig;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * Created by 12 on 2017/7/14.
 * <p>
 * FIXME 支持集群
 */
public class LockKit {


    LockKit() {
        CacheManager.init();

    }


    public String isLimit(int limit, int timeout) {
        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            return JedisRateLimiter.acquireTokenFromBucket(cache, limit, timeout);
        }
    }

    public boolean isLock(String key, int lockExpire) {
        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            return new JedisLock(cache).tryLock(key, lockExpire);
        }
    }

    public void unlock(String key) {
        try (Jedis cache = RedisConnConfig.getPool().getResource()) {
            new JedisLock(cache).unlock(key);
        }
    }


    public boolean spinLock(String key, int lockExpire) {
        return spinLock(key, lockExpire, 20);
    }

    /**
     * 自旋直到获得锁
     */
    public boolean spinLock(String key, int lockExpire, int waitMillSec) {
        if (waitMillSec < 10) waitMillSec = 10;

        while (!isLock(key, lockExpire)) {
            try {
                TimeUnit.MILLISECONDS.sleep(waitMillSec);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }


}
