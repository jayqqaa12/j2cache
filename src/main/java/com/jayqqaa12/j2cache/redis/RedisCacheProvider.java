package com.jayqqaa12.j2cache.redis;

import com.jayqqaa12.j2cache.core.Cache;
import com.jayqqaa12.j2cache.core.CacheConstans;
import com.jayqqaa12.j2cache.core.CacheProvider;
import com.jayqqaa12.j2cache.util.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Redis 缓存实现
 */
public class RedisCacheProvider implements CacheProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RedisCacheProvider.class);
    private static RedisConnConfig redisConnConfig;
    private RedisCache cache;
    private RedisPubSubListener redisPubSubListener;

    public String name() {
        return "redis";
    }

    private ExecutorService threadSubscribe;


    @Override
    public Cache buildCache(String regionName, boolean isCreate) throws CacheException {
        return cache;
    }

    @Override
    public void start() throws CacheException {

        if (redisConnConfig == null) redisConnConfig = new RedisConnConfig();
        redisConnConfig.init();

        cache = new RedisCache();
        //订阅消息频道
        threadSubscribe = Executors.newSingleThreadExecutor();
        threadSubscribe.execute(() -> {
            redisPubSubListener = new RedisPubSubListener();
            try (Jedis jedis = RedisConnConfig.getPool().getResource()) {
                jedis.subscribe(redisPubSubListener, SafeEncoder.encode(CacheConstans.REDIS_CHANNEL));
            } catch (Exception e) {
                LOG.error("J2cache exception {}",e);
            }
        });


        LOG.info(">>>>> RedisCache init success");
    }


    public static void setRedisConnConfig(RedisConnConfig redisConnConfig) {
        RedisCacheProvider.redisConnConfig = redisConnConfig;
    }

    @Override
    public void stop() {

//        redisPubSubListener.unsubscribe();
        redisPubSubListener.punsubscribe(SafeEncoder.encode(CacheConstans.REDIS_CHANNEL));
        redisPubSubListener.close();
        threadSubscribe.shutdown();
        cache.destory();

    }


}
