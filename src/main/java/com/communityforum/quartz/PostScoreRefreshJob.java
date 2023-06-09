package com.communityforum.quartz;


import com.communityforum.entity.DiscussPost;
import com.communityforum.service.DiscussPostService;
import com.communityforum.service.LikeService;
import com.communityforum.util.CommunityConstant;
import com.communityforum.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author YWH
 * @Description PostScoreRefreshJob:帖子排行
 * @Date 2023/5/5 14:47
 */
@Slf4j(topic = "PostScoreRefreshJob")
public class PostScoreRefreshJob implements Job, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    // 社区热帖排行计算起始日期
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-1-1 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化热帖排行起始日期失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            log.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        log.info("[任务开始] 正在刷新帖子分数:" + operations.size());
        while (operations.size() != 0) {
            this.refresh((Integer) operations.pop());
        }
        log.info("[任务结束] 帖子分数刷新完毕!");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            log.error("该帖子不存在, postId = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数：帖子权重+距离天数
        double score = Math.log10(Math.max(1, w)) + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
    }
}
