package com.jayqqaa12.j2cache.spring.aspect;


import com.jayqqaa12.j2cache.core.CacheConstans;
import com.jayqqaa12.j2cache.core.J2Cache;
import com.jayqqaa12.j2cache.spring.DefaultKeyGenerator;
import com.jayqqaa12.j2cache.spring.annotation.Cache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author 12
 */
@Aspect
@Service
public class CacheAspect {

    private static final Logger LOG = LoggerFactory.getLogger(CacheAspect.class);

    @Autowired
    private DefaultKeyGenerator keyParser;

    @Autowired
    private J2Cache j2Cache;


    @Pointcut("@annotation(com.jayqqaa12.j2cache.spring.annotation.Cache)")
    public void aspect() {
    }

    @Around("aspect()&&@annotation(cache)")
    public Object interceptor(ProceedingJoinPoint invocation, Cache cache)
            throws Throwable {
        MethodSignature signature = (MethodSignature) invocation.getSignature();
        Method method = signature.getMethod();
        Object result = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = invocation.getArgs();
        String key = null;
        int level = cache.level();
        String region = StringUtils.isEmpty(cache.region())?null:cache.region();

        boolean nofity = cache.notifyOther();
        try {
            key = keyParser.buildKey(cache.key(), parameterTypes, arguments);

            if (level == CacheConstans.LEVEL_ALL) result = j2Cache.get(region, key);
            else result = j2Cache.cache().get(level,region, key);

            if (result == null) {
                result = invocation.proceed();
                if (level == CacheConstans.LEVEL_ALL) j2Cache.cache().set(region,key, result, cache.expire(), nofity);
                else j2Cache.cache().set(level, region, key, result, cache.expire(), nofity);
            }
        } catch (Exception e) {
            LOG.error("获取缓存失败 {} ,{}" ,key, e);
        }
        return result;
    }


}