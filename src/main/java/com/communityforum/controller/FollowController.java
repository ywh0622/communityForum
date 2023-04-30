package com.communityforum.controller;

import com.communityforum.entity.User;
import com.communityforum.service.FollowService;
import com.communityforum.util.CommunityUtil;
import com.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author YWH
 * @Description FollowController
 * @Date 2023/4/30 13:56
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(1, "请先登陆，再进行关注!");
        }

        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "关注成功!");
    }

    @PostMapping("/unFollow")
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(1, "请先登陆，再进行关注!");
        }

        followService.unFollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "取关成功!");
    }
}
