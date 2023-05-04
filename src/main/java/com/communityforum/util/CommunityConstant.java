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

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 消息类型：已读
     */
    int MESSAGE_READ = 1;

    /**
     * 消息类型：删除
     */
    int MESSAGE_DELETE = 2;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限：普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限：管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AUTHORITY_MODERATOR = "moderator";
}
