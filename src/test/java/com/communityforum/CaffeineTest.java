package com.communityforum;

import com.communityforum.entity.DiscussPost;
import com.communityforum.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * @Author YWH
 * @Description CaffeineTest
 * @Date 2023/5/5 17:13
 */
@SpringBootTest
public class CaffeineTest {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost discussPost = new DiscussPost();
            discussPost.setUserId(111);
            discussPost.setTitle("互联网求职计划");
            discussPost.setContent("今年就业不容乐观");
            discussPost.setCreateTime(new Date());
            discussPost.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(discussPost);
        }

    }

    @Test
    public void testCache() {
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 0));
    }
}
