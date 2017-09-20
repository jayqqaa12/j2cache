package com.jayqqaa12.j2cache.spring.boot.config;

import com.jayqqaa12.j2cache.core.J2Cache;
import com.jayqqaa12.j2cache.redis.RedisCacheProvider;
import com.jayqqaa12.j2cache.redis.RedisConnConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PreDestroy;

/**
 * Created by 12 on 2017/9/20.
 */
@Configuration
@ConditionalOnClass(J2Cache.class)
@EnableConfigurationProperties({ J2CacheProperties.class})
public class J2CacheAutoConfig {



    @Bean
    @ConfigurationProperties(prefix="spring.data.redis.pool")
    public JedisPoolConfig getRedisConfig(){
        return new JedisPoolConfig();
    }


    @Bean
    @ConfigurationProperties(prefix="spring.data.redis")
    public RedisConnConfig getRedisConnConfig(){
        RedisConnConfig config = new RedisConnConfig();
        config.setPoolConfig(getRedisConfig());
        return config;
    }


    @Autowired
    RedisConnConfig redisConnConfig;



    @Bean
    @ConditionalOnMissingBean
    public J2Cache j2Cache() {

        RedisCacheProvider.setRedisConnConfig(redisConnConfig);

        return new J2Cache();
    }

    @PreDestroy
    public void close() {
        J2Cache.close();
    }
}
