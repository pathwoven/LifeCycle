package com.cc.utils;

/**
 * 访问和修改线程私有的用户信息
 * 存储了用户id
 */
public class UserHolder {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void saveUserId(long id){
        tl.set(id);
    }

    public static long getUserId(){
        return tl.get();
    }

    public static void removeUserId(){
        tl.remove();
    }
}
