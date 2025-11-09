package com.cc.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL_MIN = 2L;

    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final String LOCK_PREFIX_KEY = "lock:";
    public static final Long LOCK_TTL_SECOND = 10L;

    public static final String ID_INCREMENT_KEY = "icr:";

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_USER_KEY = "seckill:user:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final Integer SHOP_GEO_PAGE_SIZE = 20;
    public static final String USER_SIGN_KEY = "sign:";

    // feed相关
    public static final String FEED_BOX_KEY = "feed:box:";
    public static final Long FEED_BOX_TTL_DAYS = 7L;

    // 影响力
    public static final String USER_INFLUENCE_KEY = "user:influence"; // zset 用户影响力

    // 活跃度相关
    public static final String USER_ACTIVE_KEY = "user:active";   // zset 用户活跃度
    public static final Integer USER_ACTIVE_MAX = 2000;    // zset 最大存储数

    public static final String USER_FOLLOW_KEY = "user:follow:"; // set 用户关注列表
}
