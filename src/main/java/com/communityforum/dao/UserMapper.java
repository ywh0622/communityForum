package com.communityforum.dao;


import com.communityforum.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author YWH
 * @Description UserMapper
 * @Date 2023/4/19 10:47
 */
@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByName(String name);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);

}
