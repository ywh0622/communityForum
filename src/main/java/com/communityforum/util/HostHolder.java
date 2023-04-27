package com.communityforum.util;

import com.communityforum.entity.User;
import org.springframework.stereotype.Component;

/**
 * @Author YWH
 * @Description 持有用户的信息，用于代替session对象
 * @Date 2023/4/27 10:10
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUsers(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
