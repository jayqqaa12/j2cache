package com.jayqqaa12.j2cache.redis;

import com.jayqqaa12.j2cache.util.CacheException;
import com.jayqqaa12.j2cache.util.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;


public class RedisConnConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConnConfig.class);

    private String host="127.0.0.1";
    private int port =6379;
    private int timeout=2000;
    private String password;
    private JedisPoolConfig poolConfig;
    private int database=0;
    private static Pool<Jedis> pool;

    public static Pool<Jedis> getPool() {

        if(pool==null)  throw new CacheException("j2cache 未初始化成功");

        return pool;
    }

    public void init() {
        /**
         * 如果为null就读取配置文件
         */
        if (poolConfig == null) {
            LOG.warn("没有配置 JedisPoolConfig 默认使用j2cache.properties 进行初始化");

            poolConfig = new JedisPoolConfig();
            JedisPoolConfig config = new JedisPoolConfig();

            host = ConfigUtil.getStr("redis.host",null);
            password = ConfigUtil.getStr("redis.password", null);
            port = ConfigUtil.getInt("redis.port", 6379);
            timeout = ConfigUtil.getInt("redis.timeout", 2000);
            database = ConfigUtil.getInt("redis.database", 0);

            if(host==null){
                throw new CacheException("j2cache Error host 没有配置 请配置j2cache.properties");
            }


            config.setBlockWhenExhausted(ConfigUtil.getBoolean("redis.blockWhenExhausted", true));
            //最大空闲连接数
            config.setMaxIdle(ConfigUtil.getInt("redis.maxIdle", 10));
            //最小空闲连接数
            config.setMinIdle(ConfigUtil.getInt("redis.minIdle", 5));
            //最大连接数
            config.setMaxTotal(ConfigUtil.getInt("redis.maxTotal", 10000));
            //获取连接时的最大等待毫秒数
            config.setMaxWaitMillis(ConfigUtil.getInt("redis.maxWaitMillis", 100));
            //逐出连接的最小空闲时间
            config.setMinEvictableIdleTimeMillis(ConfigUtil.getInt("redis.minEvictableIdleTimeMillis", 1000));
            //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
            config.setSoftMinEvictableIdleTimeMillis(ConfigUtil.getInt("redis.softMinEvictableIdleTimeMillis", 10));
            //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认10
            config.setNumTestsPerEvictionRun(ConfigUtil.getInt("redis.numTestsPerEvictionRun", 10));
            //逐出扫描的时间间隔(毫秒)如果为负数,则不运行逐出线程, 默认-1
            config.setTimeBetweenEvictionRunsMillis(ConfigUtil.getInt("redis.timeBetweenEvictionRunsMillis", 100));
            //在空闲时检查有效性, 默认false
            config.setTestWhileIdle(ConfigUtil.getBoolean("redis.testWhileIdle", false));
            //在获取连接的时候检查有效性, 默认false
            config.setTestOnBorrow(ConfigUtil.getBoolean("redis.testOnBorrow", true));
            config.setTestOnReturn(ConfigUtil.getBoolean("redis.testOnReturn", false));
            config.setLifo(ConfigUtil.getBoolean("redis.lifo", false));
        }
        else {
            LOG.warn("加载 配置的 JedisPoolConfig 进行初始化");
        }


        if (StringUtils.isEmpty(password)) {
            pool = new JedisPool(poolConfig, host, port, timeout);
        } else {
            pool = new JedisPool(poolConfig, host, port, timeout, password, database);
        }
    }


    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setDatabase(int database) {
        this.database = database;
    }
}
