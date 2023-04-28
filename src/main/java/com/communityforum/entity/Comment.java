package com.communityforum.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author YWH
 * @Description 评论实体
 * @Date 2023/4/28 14:47
 */
@Data
public class Comment {
    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;
}
