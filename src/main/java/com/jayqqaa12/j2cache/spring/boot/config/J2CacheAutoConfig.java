package com.jayqqaa12.j2cache.spring.boot.config;

import com.jayqqaa12.j2cache.J2Cache;
import com.jayqqaa12.j2cache.redis.RedisCacheProvider;
import com.jayqqaa12.j2cache.redis.RedisConnConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by 12 on 2017/9/20.
 */
@Configuration
@ConditionalOnClass(J2Cache.class)
@EnableConfigurationProperties({SpringBootRedisConfig.class})
@EnableAspectJAutoProxy(exposeProxy = true,proxyTargetClass = true)
@ComponentScan("com.jayqqaa12.j2cache.spring")
public class J2CacheAutoConfig {

    @Autowired
    SpringBootRedisConfig springBootRedisConfig;

    @Bean
    @ConditionalOnMissingBean
    public J2Cache j2Cache() {

        RedisConnConfig redisConnConfig = new RedisConnConfig();
        redisConnConfig.setPoolConfig(new JedisPoolConfig());
        BeanUtils.copyProperties(springBootRedisConfig, redisConnConfig);

        RedisCacheProvider.setRedisConnConfig(redisConnConfig);

        return new J2Cache();
    }





}
