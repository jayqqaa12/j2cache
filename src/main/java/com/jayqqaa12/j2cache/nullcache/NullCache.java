package com.jayqqaa12.j2cache.nullcache;

import com.jayqqaa12.j2cache.core.Cache;
import com.jayqqaa12.j2cache.util.CacheException;

import java.util.List;


public class NullCache implements Cache {

    @Override
    public List<Object> keys(String region) {
        return null;
    }

    public Object get(String region, String key) throws CacheException {
        return null;
    }


    public void set(String region, String key, Object value, int seconds) throws CacheException {

    }


    public void remove(String region, Object key) throws CacheException {

    }


    public void clear(String region) throws CacheException {

    }


    public Object exprie(String region, String key, int seconds) {
        return null;
    }
}
