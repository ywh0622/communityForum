package com.communityforum.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author YWH
 * @Description Message
 * @Date 2023/4/28 20:36
 */
@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}
