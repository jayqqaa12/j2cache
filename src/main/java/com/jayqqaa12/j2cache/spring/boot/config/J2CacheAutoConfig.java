package com.jayqqaa12.j2cache.spring.boot.config;

import com.jayqqaa12.j2cache.J2Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

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


        //FIXME
//        RedisCacheProvider.setRedisConnConfig(redisConnConfig);

        return new J2Cache();
    }





}
