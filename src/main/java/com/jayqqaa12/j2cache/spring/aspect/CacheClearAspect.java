package com.jayqqaa12.j2cache.spring.aspect;


import com.jayqqaa12.j2cache.core.J2Cache;
import com.jayqqaa12.j2cache.spring.SpelKeyGenerator;
import com.jayqqaa12.j2cache.spring.annotation.CacheClear;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author 12
 */
@Aspect
@Service
public class CacheClearAspect {

    @Autowired
    private SpelKeyGenerator keyParser;
    @Autowired
    private J2Cache j2Cache;

    @Pointcut("@annotation(com.jayqqaa12.j2cache.spring.annotation.CacheClear)")
    public void aspect() {
    }

    @Around("aspect()&&@annotation(cacheClear)")
    public Object interceptor(ProceedingJoinPoint invocation, CacheClear cacheClear)
            throws Throwable {

        String region = StringUtils.isEmpty(cacheClear.region()) ? null : cacheClear.region();

        if (StringUtils.isNotBlank(cacheClear.key())) {
            String key = keyParser.buildKey(cacheClear.key(), invocation);

            // 可能为数组
            if (key.contains(",")) j2Cache.remove(region, Arrays.asList(key.split(",")));
            else j2Cache.remove(region, key);

        }

        return invocation.proceed();
    }


}
