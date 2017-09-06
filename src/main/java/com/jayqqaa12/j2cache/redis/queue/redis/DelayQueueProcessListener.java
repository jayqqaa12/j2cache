package com.jayqqaa12.j2cache.redis.queue.redis;


import com.jayqqaa12.j2cache.redis.queue.core.Message;

public interface DelayQueueProcessListener<T> {

    void ackCallback(Message<T>  message);

    void peekCallback(Message<T>  message);

    void pushCallback(Message<T>  message);
}
