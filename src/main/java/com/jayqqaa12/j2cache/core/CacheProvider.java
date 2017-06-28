package com.jayqqaa12.j2cache.core;

import com.jayqqaa12.j2cache.util.CacheException;


public interface CacheProvider {

    String name();

    Cache buildCache(String regionName ) throws CacheException;

    void start() throws CacheException;

    void stop();

}
