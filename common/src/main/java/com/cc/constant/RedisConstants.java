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

    // 0 正在生成订单, 1 待支付，2 支付完成 3 已核销 4 已取消 5 退款中；6 已退款 -1 库存不足 / 秒杀失败
    // 0->1,-1   1->2,4    2->4（订单生成时不可取消）
    public static final String VOUCHER_ORDER_STATUS_KEY = "voucher-order:status:";
    public static final Long SECKILL_ORDER_TTL_MIN = 15L;

    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String BLOG_HOT_CITY_KEY = "blog:hot:";   // zset 博客热门城市排行榜


    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final Integer SHOP_GEO_PAGE_SIZE = 20;
    public static final String USER_SIGN_KEY = "sign:";

    // feed相关
    public static final String FEED_BOX_KEY = "feed:box:";    // 关注的人的博客id列表
    public static final Long FEED_BOX_TTL_DAYS = 7L;
    public static final String FEED_AUTHOR_KEY = "feed:author:";  // list 大V发布的动态
    public static final Integer FEED_AUTHOR_LIST_MAX = 10;
    public static final int FEED_PAGE_SIZE = 10;
    public static final int FEED_PAGE_FOLLOW_SIZE = 5;
    public static final String FEED_REED_KEY = "feed:read:"; // set 用户已读动态

    // 影响力
    public static final String USER_INFLUENCE_KEY = "user:influence"; // zset 用户影响力

    // 活跃度相关
    public static final String USER_ACTIVE_KEY = "user:active";   // zset 用户活跃度
    public static final Integer USER_ACTIVE_MAX = 2000;    // zset 最大存储数

    public static final String USER_FOLLOW_KEY = "user:follow:"; // set 用户关注列表
}
