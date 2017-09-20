package com.jayqqaa12.j2cache.spring.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 12 on 2017/9/20.

 */
@ConfigurationProperties(prefix = "j2cache")
public class J2CacheProperties {

    private Long defultTimeout;
    private String  l1Provider="ehcache";
    private String  l2Provider="redis";




    public Long getDefultTimeout() {
        return defultTimeout;
    }

    public void setDefultTimeout(Long defultTimeout) {
        this.defultTimeout = defultTimeout;
    }

    public String getL1Provider() {
        return l1Provider;
    }

    public void setL1Provider(String l1Provider) {
        this.l1Provider = l1Provider;
    }

    public String getL2Provider() {
        return l2Provider;
    }

    public void setL2Provider(String l2Provider) {
        this.l2Provider = l2Provider;
    }
}
