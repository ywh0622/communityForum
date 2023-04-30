package com.communityforum.dao;

import com.communityforum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author YWH
 * @Description CommentMapper
 * @Date 2023/4/28 14:49
 */
@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    int insertComment(Comment comment);

    List<Comment> selectCommentsByUserIdAndTEntityType(@Param("userId") int userId, @Param("entityType") int entityType, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByUserIdAndTEntityType(@Param("userId") int userId, @Param("entityType") int entityType);
}
