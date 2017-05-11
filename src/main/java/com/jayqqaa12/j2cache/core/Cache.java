package com.jayqqaa12.j2cache.core;

import com.jayqqaa12.j2cache.util.CacheException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * copy
 * Implementors define a caching algorithm. All implementors
 * <b>must</b> be threadsafe.
 */
public interface Cache {


    /**
     * get all keys the cache region
     *
     * @param region
     * @return
     */
    List<Object> keys(String region);

    /**
     * Get an item from the cache
     *
     * @param region cache region
     * @param key    cache key
     * @return the cached object or null
     */
    Object get(String region, Serializable key) throws CacheException;

    /**
     * Add an item to the cache
     * failfast semantics
     *
     * @param region
     * @param key     cache key
     * @param value   cache value
     * @param seconds cache Expiration time
     */
    void set(String region, Serializable key, Object value, int seconds) throws CacheException;

    void pset(String region, Map<Serializable, Object> data, int seconds) throws CacheException;

    /**
     * @param key Cache key
     *            Remove an item from the cache
     */
    void remove(String region, Object key) throws CacheException;

    /**
     * Clear the cache
     */
    void clear(String region) throws CacheException;

    /**
     * update exprie time
     *
     * @param key
     * @param seconds
     */
    Object exprie(String region, Serializable key, int seconds);

}
