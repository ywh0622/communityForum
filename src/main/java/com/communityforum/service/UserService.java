package com.communityforum.service;

import com.communityforum.dao.LoginTicketMapper;
import com.communityforum.dao.UserMapper;
import com.communityforum.entity.LoginTicket;
import com.communityforum.entity.User;
import com.communityforum.util.CommunityConstant;
import com.communityforum.util.CommunityUtil;
import com.communityforum.util.HostHolder;
import com.communityforum.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${communityForum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("输入的账号不能为空");
        }

        Map<String, Object> map = new HashMap<>();
        // 判断空值
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "账号已存在！");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已存在！");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:80/communityForum/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
//        System.out.println("激活地址：" + url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * 激活账号
     *
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 登陆逻辑
     *
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("passwordMsg", "密码错误!");
            return map;
        }

        // 登陆成功 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 修改登陆凭证状态为退出
     *
     * @param ticket
     */
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    /**
     * 重置密码前验证邮箱
     *
     * @param email
     * @return
     */
    public Map<String, Object> verifyEmail(String email) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱未被注册，请重新输入!");
            return map;
        } else {
            Context context = new Context();
            context.setVariable("email", email);
            String code = CommunityUtil.generateUUID().substring(0, 4);
            context.setVariable("verifyCode", code);
            String content = templateEngine.process("/mail/forget", context);
            mailClient.sendMail(email, "找回密码", content);

            // 保存验证码
            map.put("verifyCode", code);
            return map;
        }
    }

    /**
     * 找回密码
     *
     * @param email
     * @param password
     * @return
     */
    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }

        // 重置密码
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);

        map.put("user", user);
        return map;
    }

    /**
     * 查询登陆凭证
     *
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    /**
     * 更改头像
     *
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public Map<String, Object> changePassword(String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        // 判断空值
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空");
            return map;
        }

        // 获取当前用户
        User user = hostHolder.getUser();
        if (user != null) {
            // 用户不为空且旧密码正确
            if (user.getPassword().equals(CommunityUtil.md5(oldPassword + user.getSalt()))) {
                // 修改密码
                if (oldPassword.equals(newPassword)) {
                    map.put("newPasswordMsg", "旧密码不能与新密码一致!");
                    return map;
                } else {
                    userMapper.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));
                    map.put("success", "密码修改成功!");
                }
            } else {
                map.put("oldPasswordMsg", "原密码错误,请重新输入!");
            }
        } else {
            // 这是避免用户之前在地址栏输入setting来访问该页面，此时未登陆，所以threadlocal里面没有对应的用户，
            // 后续会添加拦截器，未登陆的用户无法访问该页面, 此时最外层if判断可删去
            map.put("oldPasswordMsg", "用户未登陆");
        }
        return map;
    }

    /**
     * 获取目标私信用户
     *
     * @param conversationId
     * @return
     */
    public User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userMapper.selectById(id1);
        } else {
            return userMapper.selectById(id0);
        }
    }
}
