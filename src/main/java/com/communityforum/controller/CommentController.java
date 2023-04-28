package com.communityforum.controller;

import com.communityforum.annotation.LoginRequired;
import com.communityforum.entity.Comment;
import com.communityforum.service.CommentService;
import com.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @Author YWH
 * @Description CommentController
 * @Date 2023/4/28 16:35
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @PostMapping("/add/{discussPostId}")
    @LoginRequired
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        // 前端的comment会传入content，entityType(1)和entityId(帖子Id)
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.addComment(comment);
        // 评论完成之后，重定向回当前帖子页面
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
