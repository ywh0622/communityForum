package com.communityforum;


import com.communityforum.dao.*;
import com.communityforum.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    void testSelectUser() {
        User user = userMapper.selectById(150);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("niuke@sina.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    void testDiscussPost() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost post : discussPosts) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostsRows(149);
        System.out.println(rows);
    }

    @Test
    void testLoginInsert(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("123");
        loginTicket.setStatus(1);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    void testLoginSelect(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("123");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("123",0);
        loginTicket = loginTicketMapper.selectByTicket("123");
        System.out.println(loginTicket);
    }

    @Test
    void testAddComment(){
        Comment comment = new Comment();
        comment.setContent("nihao");
        comment.setUserId(155);
        comment.setEntityType(1);
        comment.setEntityId(155);
        comment.setTargetId(0);
        comment.setStatus(2);
        comment.setCreateTime(new Date());

        int row = commentMapper.insertComment(comment);
        System.out.println(row);
    }

    @Test
    void testMessage(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        messages.forEach(System.out::println);
        System.out.println();

        int conversationCount = messageMapper.selectConversationCount(111);
        System.out.println(conversationCount);
        System.out.println();

        List<Message> letters = messageMapper.selectLetters("111_112", 0, 10);
        letters.forEach(System.out::println);
        System.out.println();

        int letterCount = messageMapper.selectLetterCount("111_112");
        System.out.println(letterCount);
        System.out.println();

        int selectLetterUnreadCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(selectLetterUnreadCount);
    }
}
