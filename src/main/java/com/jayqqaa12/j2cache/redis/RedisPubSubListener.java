package com.jayqqaa12.j2cache.redis;

import com.jayqqaa12.j2cache.core.CacheManager;
import com.jayqqaa12.j2cache.util.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedisPubSub;

import java.util.List;

/**
 * FIXME 还有问题
 *
 * 监听删除操作，删除1级缓存
 *
 */
public class RedisPubSubListener extends BinaryJedisPubSub {

    private final static Logger log = LoggerFactory.getLogger(RedisPubSubListener.class);
    private boolean stop;

    @Override
    public void onMessage(byte[] channel, byte[] message) {
        // 无效消息
        if (message != null && message.length <= 0) {
            log.warn("Message is empty.");
            return;
        }
        try {
            Command cmd = Command.parse(message);
            if (cmd == null || cmd.isLocalCommand())
                return;

            switch (cmd.getOperator()) {
                case Command.OPT_DELETE_KEY:
                    onDeleteCacheKey(cmd.getRegion(), cmd.getKey());
                    break;
                case Command.OPT_CLEAR_KEY:
                    onClearCacheKey(cmd.getRegion());
                    break;
                default:
                    log.warn("Unknown message type = " + cmd.getOperator());
            }
        } catch (Exception e) {
            log.error("Unable to handle received msg", e);
        }
    }

    public  void close(){
        stop=true;
    }

    public boolean isSubscribed() {
        if(stop)return false;
        else return super.isSubscribed();
    }

    /**
     * 删除一级缓存的键对应内容
     *
     * @param region : Cache region name
     * @param key    : cache key
     */
    protected void onDeleteCacheKey(String region, Object key) {
        if (key instanceof List)
            CacheManager.batchRemove(1, region, (List) key);
        else
            CacheManager.remove(1, region, key.toString());
        log.debug("Received cache evict message, region=" + region + ",key=" + key);
    }

    /**
     * 清除一级缓存的键对应内容
     *
     * @param region Cache region name
     */
    protected void onClearCacheKey(String region) {
        CacheManager.clear(1, region);
        log.debug("Received cache clear message, region=" + region);
    }

}