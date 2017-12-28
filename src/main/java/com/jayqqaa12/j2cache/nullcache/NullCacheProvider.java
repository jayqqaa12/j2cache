package com.jayqqaa12.j2cache.nullcache;

import com.jayqqaa12.j2cache.Cache;
import com.jayqqaa12.j2cache.CacheProvider;
import com.jayqqaa12.j2cache.util.CacheException;


public class NullCacheProvider implements CacheProvider {

    private final static NullCache cache = new NullCache();

    @Override
    public String name() {
        return "none";
    }


    @Override
    public Cache buildCache(String regionName, boolean isCreate) throws CacheException {
        return cache;
    }


    @Override
    public void start( ) throws CacheException {
    }

    @Override
    public void stop() {
    }

}
