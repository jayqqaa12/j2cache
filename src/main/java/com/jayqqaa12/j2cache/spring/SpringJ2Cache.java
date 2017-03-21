package com.jayqqaa12.j2cache.spring;


import org.springframework.cache.Cache;

/**
 * TODO  spring-cache 实现
 *
 */
public class SpringJ2Cache implements Cache {
    @Override
    public String getName() {
        return "j2cache";
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public ValueWrapper get(Object o) {
        return null;
    }

    @Override
    public <T> T get(Object o, Class<T> aClass) {
        return null;
    }

    @Override
    public void put(Object o, Object o1) {

    }

    @Override
    public ValueWrapper putIfAbsent(Object o, Object o1) {
        return null;
    }

    @Override
    public void evict(Object o) {

    }

    @Override
    public void clear() {

    }
}
