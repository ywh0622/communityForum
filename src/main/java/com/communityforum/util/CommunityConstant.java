package com.communityforum.util;

/**
 * @Author YWH
 * @Description 定义常量
 * @Date 2023/4/26 16:00
 */
public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登陆凭证的超时时间
     */
    long DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登陆凭证的超时时间
     */
    long REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 7;
}
