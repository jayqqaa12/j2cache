package com.jayqqaa12.j2cache.spring.aspect;


import com.jayqqaa12.j2cache.core.J2Cache;
import com.jayqqaa12.j2cache.spring.DefaultKeyGenerator;
import com.jayqqaa12.j2cache.spring.annotation.CacheClear;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * @author 12
 */
@Aspect
@Service
public class CacheClearAspect {

    @Autowired
    private DefaultKeyGenerator keyParser;
    @Autowired
    private J2Cache j2Cache;

    @Pointcut("@annotation(com.jayqqaa12.j2cache.spring.annotation.CacheClear)")
    public void aspect() {
    }

    @Around("aspect()&&@annotation(cacheClear)")
    public Object interceptor(ProceedingJoinPoint invocation, CacheClear cacheClear)
            throws Throwable {
        MethodSignature signature = (MethodSignature) invocation.getSignature();
        Method method = signature.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = invocation.getArgs();
        String region = StringUtils.isEmpty(cacheClear.region())?null:cacheClear.region();

        if (StringUtils.isNotBlank(cacheClear.key())) {
            String key = keyParser.buildKey(cacheClear.key(), parameterTypes, arguments);
            j2Cache.remove(region, key);
        }

        return invocation.proceed();
    }


}
