package com.communityforum.service;

import com.communityforum.dao.CommentMapper;
import com.communityforum.entity.Comment;
import com.communityforum.entity.DiscussPost;
import com.communityforum.util.CommunityConstant;
import com.communityforum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author YWH
 * @Description CommentService
 * @Date 2023/4/28 14:57
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 添加评论
        // 内容过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    // 根据用户id，查询该用户针对帖子的所有评论
    public List<Comment> getDiscussPostByComment(int userId, int offset, int limit) {
        return commentMapper.selectCommentsByUserIdAndTEntityType(userId, ENTITY_TYPE_POST, offset, limit);
    }

    // 根据用户id，查询该用户评论的帖子数量
    public int getDiscussPostCountByComment(int userId) {
        return commentMapper.selectCountByUserIdAndTEntityType(userId, ENTITY_TYPE_POST);
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
