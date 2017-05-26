package com.jayqqaa12.j2cache.core;

import com.jayqqaa12.j2cache.util.ConfigUtil;

/**
 * 统一用常量获取
 */
public class CacheConstans {
    //配置文件名字
    public final static String CONFIG_FILE = "j2cache";//可以不带.properties
    public final static String EHCACHE = "ehcache";
    public final static String REDIS = "redis";
    public final static String EHCACHE_DEFAULT_REGION ="__DEFAULT__";
    //redis channel消息订阅默认频道
    public final static String REDIS_CHANNEL = "__DEFAULT__";

    public final static String L1_PROVIDER = ConfigUtil.getStr("cache.L1.provider");
    public final static String L2_PROVIDER = ConfigUtil.getStr("cache.L2.provider");
    //默认存储时间
    public final static int DEFAULT_L1_TIME = Integer.valueOf(ConfigUtil.getStr("cache.L1.defaultTime"));

    public final static int DEFAULT_TIME = Integer.valueOf(ConfigUtil.getStr("cache.defaultTime"));

    //redis时前缀加namespace,多个子项目更清晰redis
    public final static String NAMESPACE = ConfigUtil.getStr("cache.namespace");
    //空的RGION
    public static String NUllRegion = null;



}
