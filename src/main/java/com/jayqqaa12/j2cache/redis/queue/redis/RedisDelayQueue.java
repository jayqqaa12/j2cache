package com.jayqqaa12.j2cache.redis.queue.redis;

import com.alibaba.fastjson.JSON;
import com.jayqqaa12.j2cache.redis.RedisConnConfig;
import com.jayqqaa12.j2cache.redis.queue.core.DelayQueue;
import com.jayqqaa12.j2cache.redis.queue.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.sortedset.ZAddParams;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class RedisDelayQueue implements DelayQueue {
    private static final Logger LOG = LoggerFactory.getLogger(RedisDelayQueue.class);

    private transient final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();
    private JedisPool jedisPool= RedisConnConfig.getPool();
    private long MAX_TIMEOUT = 525600000; // 最大超时时间不能超过一年
    private int unackTime = 60 * 1000;
    private String redisKeyPrefix;
    private String messageStoreKey;
    private String realQueueName;

    private DelayQueueProcessListener delayQueueProcessListener;

    private volatile boolean isEmpty = false;
    private volatile boolean status = true;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RedisDelayQueue(String redisKeyPrefix, DelayQueueProcessListener delayQueueProcessListener) {
        this.redisKeyPrefix = redisKeyPrefix;
        this.messageStoreKey = redisKeyPrefix + ":delay_msg";
        this.realQueueName = redisKeyPrefix + ":delay_queue";
        this.delayQueueProcessListener = delayQueueProcessListener;
    }


    @Override
    public boolean push(Message message) {
        if (message.getCreateTime() == null || message.getTimeout() > MAX_TIMEOUT) {
            throw new IllegalArgumentException("Maximum delay time should not be exceed one year");
        }
        if(message.getId()==null){
            throw new IllegalArgumentException("message id can't is null");
        }


        try (Jedis jedis = jedisPool.getResource()) {
            String json = JSON.toJSONString(message);
            jedis.hset(messageStoreKey, message.getId(), json);
            double priority = message.getPriority() / 100;
            double score = Long.valueOf(System.currentTimeMillis() + message.getTimeout()).doubleValue() + priority;
            jedis.zadd(realQueueName, score, message.getId());
            delayQueueProcessListener.pushCallback(message);
            isEmpty = false;
            return true;
        } catch (Exception e) {
            LOG.error(" redis queue error {}", e);
        }
        return false;
    }

    public void listen() {
        executorService.execute(() -> {
            while (status) {
                String id = peekId();
                if (id == null) continue;

                try (Jedis jedis = jedisPool.getResource()) {
                    String json = jedis.hget(messageStoreKey, id);
                    Message message = JSON.parseObject(json, Message.class);

                    if (message == null) continue;
                    long delay = message.getCreateTime() + message.getTimeout() - System.currentTimeMillis();


                    LOG.info("CREATE TIME  {}  past date {} ", new Date(message.getCreateTime()).toLocaleString(), new Date(message.getCreateTime() + message.getTimeout()).toLocaleString());

                    if (delay <= 0) {
                        delayQueueProcessListener.peekCallback(message);
                    } else {
                        LockSupport.parkNanos(this, TimeUnit.NANOSECONDS.convert(delay, TimeUnit.MILLISECONDS));
                        delayQueueProcessListener.peekCallback(message);
                    }
                    ack(message.getId());
                } catch (Exception e) {
                    LOG.error(" redis queue error {}", e);
                }
            }
        });

    }


    @Override
    public void close() {
        status = false;
        executorService.shutdown();
        LOG.info("redis delay queue is exist ");
    }


    @Override
    public boolean ack(String messageId) {
        String unackQueueName = getUnackQueueName();

        try (Jedis jedis = jedisPool.getResource()) {

            jedis.zrem(unackQueueName, messageId);
            Long removed = jedis.zrem(realQueueName, messageId);
            Long msgRemoved = jedis.hdel(messageStoreKey, messageId);

            LOG.debug("ack msgid {}, zset {} hash {}", messageId, removed, msgRemoved);
            if (removed > 0 && msgRemoved > 0) {
                return true;
            }

        }
        return false;

    }

    @Override
    public boolean setUnackTimeout(String messageId, long timeout) {
        double unackScore = Long.valueOf(System.currentTimeMillis() + timeout).doubleValue();
        String unackQueueName = getUnackQueueName();

        try (Jedis jedis = jedisPool.getResource()) {

            Double score = jedis.zscore(unackQueueName, messageId);
            if (score == null) {
                jedis.zadd(unackQueueName, unackScore, messageId);
                return true;
            }

        }
        return false;

    }

    @Override
    public boolean setTimeout(String messageId, long timeout) {
        try (Jedis jedis = jedisPool.getResource()) {


            String json = jedis.hget(messageStoreKey, messageId);
            if (json == null) {
                return false;
            }
            Message message = JSON.parseObject(json, Message.class);
            message.setTimeout(timeout);
            Double score = jedis.zscore(realQueueName, messageId);
            if (score != null) {
                double priorityd = message.getPriority() / 100;
                double newScore = Long.valueOf(System.currentTimeMillis() + timeout).doubleValue() + priorityd;
                ZAddParams params = ZAddParams.zAddParams().xx();
                long added = jedis.zadd(realQueueName, newScore, messageId, params);
                if (added == 1) {
                    json = JSON.toJSONString(message);
                    jedis.hset(messageStoreKey, message.getId(), json);
                    return true;
                }
                return false;
            }
            return false;
        } catch (Exception e) {
            LOG.error(" redis queue error {}", e);
            ;
            return false;
        }
    }

    @Override
    public Message get(String messageId) {

        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget(messageStoreKey, messageId);
            if (json == null) return null;
            return JSON.parseObject(json, Message.class);
        } catch (Exception e) {
            LOG.error(" redis queue error {}", e);
            return null;
        }
    }


    @Override
    public boolean contain(String messageId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists(messageStoreKey, messageId);
        } catch (Exception e) {
            LOG.error(" redis queue error {}", e);
        }
        return false;
    }

    @Override
    public long size() {

        try (Jedis jedis = jedisPool.getResource()) {

            return jedis.zcard(realQueueName);
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {

            jedis.del(realQueueName);
            jedis.del(getUnackQueueName());
            jedis.del(messageStoreKey);

        }
    }

    private String peekId() {
        try {
            if (!isEmpty) {
                lock.lockInterruptibly();
                double max = Long.valueOf(System.currentTimeMillis() + MAX_TIMEOUT).doubleValue();
                try (Jedis jedis = jedisPool.getResource()) {
                    Set<String> scanned = jedis.zrangeByScore(realQueueName, 0, max, 0, 1);
                    if (scanned.size() > 0) {
                        String messageId = scanned.toArray()[0].toString();
                        jedis.zrem(realQueueName, messageId);
                        setUnackTimeout(messageId, unackTime);
                        if (size() == 0) isEmpty = true;
                        available.signal();
                        lock.unlock();
                        return messageId;
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.error(" redis queue error {}", e);
            available.signal();
            lock.unlock();
        }
        return null;
    }

    public void processUnacks() {

        int batchSize = 1_000;
        String unackQueueName = getUnackQueueName();
        double now = Long.valueOf(System.currentTimeMillis()).doubleValue();

        try (Jedis jedis = jedisPool.getResource()) {

            Set<Tuple> unacks = jedis.zrangeByScoreWithScores(unackQueueName, 0, now, 0, batchSize);
            for (Tuple unack : unacks) {
                double score = unack.getScore();
                String member = unack.getElement();
                String payload = jedis.hget(messageStoreKey, member);
                if (payload == null) {
                    jedis.zrem(unackQueueName, member);
                    continue;
                }
                jedis.zadd(realQueueName, score, member);
                jedis.zrem(unackQueueName, member);
            }

        }

    }

    private String getUnackQueueName() {
        return redisKeyPrefix + ":delay_unack";
    }

    @Override
    public String getName() {
        return this.realQueueName;
    }


    @Override
    public int getUnackTime() {
        return this.unackTime;
    }

    @Override
    public void setUnackTime(int time) {
        this.unackTime=time;
    }

}
