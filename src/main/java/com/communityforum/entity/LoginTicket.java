package com.communityforum.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author YWH
 * @Description 登陆凭证
 * @Date 2023/4/26 23:34
 */
@Data
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
}
