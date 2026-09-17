package com.swl.jikeai.ratelimiter.enums;

/**
 * 限流枚举类
 */
public enum RateLimitType {

    /**
     * 接口级别限流
     */
    API,

    /**
     * 用户级别限流
     */
    USER,

    /**
     * IP级别限流
     */
    IP;
}
