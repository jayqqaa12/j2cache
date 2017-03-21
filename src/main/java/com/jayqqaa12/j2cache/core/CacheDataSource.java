package com.jayqqaa12.j2cache.core;

@FunctionalInterface
public interface CacheDataSource<T> {

    T load();

}
