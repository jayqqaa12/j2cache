package com.jayqqaa12.j2cache.redis;

import com.jayqqaa12.j2cache.core.Cache;
import com.jayqqaa12.j2cache.core.CacheConstans;
import com.jayqqaa12.j2cache.core.CacheProvider;
import com.jayqqaa12.j2cache.util.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Redis 缓存实现
 */
public class RedisCacheProvider implements CacheProvider {
    private static RedisConnConfig redisConnConfig;
    private RedisCache cache;
    private RedisPubSubListener redisPubSubListener;

    public String name() {
        return "redis";
    }

    private ExecutorService threadSubscribe;

    @Override
    public Cache buildCache(String regionName) throws CacheException {
        return cache;
    }

    @Override
    public void start() throws CacheException {

        if (redisConnConfig == null) redisConnConfig = new RedisConnConfig();

        cache = new RedisCache(redisConnConfig);

        //订阅消息频道
        threadSubscribe = Executors.newSingleThreadExecutor();
        threadSubscribe.execute(() -> {
            redisPubSubListener = new RedisPubSubListener();
            try (Jedis jedis = RedisConnConfig.getPool().getResource()) {
                jedis.subscribe(redisPubSubListener, SafeEncoder.encode(CacheConstans.REDIS_CHANNEL));
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }


    public void setRedisConnConfig(RedisConnConfig redisConnConfig) {
        RedisCacheProvider.redisConnConfig = redisConnConfig;
    }

    @Override
    public void stop() {

        redisPubSubListener.punsubscribe(SafeEncoder.encode(CacheConstans.REDIS_CHANNEL));
        redisPubSubListener.close();
        threadSubscribe.shutdown();
        cache.destory();

    }


}
