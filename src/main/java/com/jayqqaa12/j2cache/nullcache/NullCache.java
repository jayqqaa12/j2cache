package com.jayqqaa12.j2cache.nullcache;

import com.jayqqaa12.j2cache.core.Cache;
import com.jayqqaa12.j2cache.util.CacheException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NullCache implements Cache {

    @Override
    public List<Object> keys(String region) {
        return null;
    }

    @Override
    public Object get(String region, Serializable key) throws CacheException {
        return null;
    }

    @Override
    public void set(String region, Serializable key, Object value, int seconds) throws CacheException {

    }

    @Override
    public void batchSet(String region, Map<Serializable, Object> data, int seconds) throws CacheException {

    }

    @Override
    public List<Object> batchGet(String region) throws CacheException {
        return new ArrayList<>();
    }


    public void remove(String region, Object key) throws CacheException {

    }


    public void clear(String region) throws CacheException {

    }


    public Object exprie(String region, Serializable key, int seconds) {
        return null;
    }
}
