package com.communityforum.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author YWH
 * @Description 用户信息
 * @Date 2023/4/19 10:43
 */
@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}
