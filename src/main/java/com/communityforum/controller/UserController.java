package com.communityforum.controller;

import com.communityforum.annotation.LoginRequired;
import com.communityforum.entity.Comment;
import com.communityforum.entity.DiscussPost;
import com.communityforum.entity.Page;
import com.communityforum.entity.User;
import com.communityforum.service.*;
import com.communityforum.util.CommunityConstant;
import com.communityforum.util.CommunityUtil;
import com.communityforum.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author YWH
 * @Description 用户信息相关操作
 * @Date 2023/4/27 10:39
 */
@Controller
@RequestMapping("/user")
@Slf4j(topic = "UserController")
public class UserController implements CommunityConstant {

    @Value("${communityForum.path.upload}")
    private String uploadPath;

    @Value("${communityForum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private FollowService followService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/setting")
//    @LoginRequired
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 上传头像
     *
     * @param headerImage
     * @param model
     * @return
     */
    @PostMapping("/upload")
//    @LoginRequired
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        //获取传入的图片名称
        String fileName = headerImage.getOriginalFilename();
        //获取图片后缀格式
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + "." + suffix;
        // 确定文件的存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("上传头像失败:" + e.getMessage());
            throw new RuntimeException("上传头像文件失败，服务器异常", e);
        }

        // 更新当前用户的头像路径(web访问路径)
        // http://localhost:80/communityForum/user/header/xxx.jpg
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     * 获取头像
     *
     * @param fileName
     * @param response
     */
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 向浏览器输出图片
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (FileNotFoundException e) {
            log.error("服务器中未找到该图片：" + e.getMessage());
        } catch (IOException e) {
            log.error("读取头像失败" + e.getMessage());
        }
    }

    /**
     * 修改密码功能
     *
     * @param oldPassword
     * @param newPassword
     * @param model
     * @return
     */
    @PostMapping("/changePassword")
//    @LoginRequired
    public String changePassword(String oldPassword, String newPassword, Model model) {
        Map<String, Object> map = userService.changePassword(oldPassword, newPassword);
        if (map.containsKey("success")) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    /**
     * 用户主页-个人信息
     *
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        // 用户主页中，判断是显示我的帖子还是TA的帖子; 判断是显示我的回复还是TA的回复
        boolean isMyPost = false, isMyReply = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
            isMyPost = hostHolder.getUser().getId() == userId;
            isMyReply = hostHolder.getUser().getId() == userId;
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("isMyPost", isMyPost);
        model.addAttribute("isMyReply", isMyReply);

        return "/site/profile";
    }

    /**
     * 个人主页中我的帖子部分
     *
     * @param userId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/myPost/{userId}")
    public String getMyPostPage(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        // 用户
        model.addAttribute("user", user);

        // 分页信息
        page.setLimit(5);
        int discussPostsCount = discussPostService.findDiscussPostRows(userId);
        page.setRows(discussPostsCount);
        page.setPath("/user/myPost/" + userId);

        List<DiscussPost> discussPostList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 1);
        List<Map<String, Object>> discussPostVOList = new ArrayList<>();
        if (discussPostList != null) {
            for (DiscussPost post : discussPostList) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPostVOList.add(map);
            }
        }
        model.addAttribute("discussPostVOList", discussPostVOList);
        model.addAttribute("discussPostsCount", discussPostsCount);

        // 用户主页中，判断是显示我的帖子还是TA的帖子; 判断是显示我的回复还是TA的回复
        boolean isMyPost = false, isMyReply = false;
        if (hostHolder.getUser() != null) {
            isMyPost = hostHolder.getUser().getId() == userId;
            isMyReply = hostHolder.getUser().getId() == userId;
        }
        model.addAttribute("isMyPost", isMyPost);
        model.addAttribute("isMyReply", isMyReply);

        return "/site/my-post";
    }

    /**
     * 个人主页中，我的回复页面
     *
     * @param userId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/myReply/{userId}")
    public String getMyReplyPage(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 用户
        model.addAttribute("user", user);

        // 分页设置
        page.setLimit(5);
        int discussPostCount = commentService.getDiscussPostCountByComment(userId);
        page.setRows(discussPostCount);
        page.setPath("/user/myReply/" + userId);

        List<Comment> commentList = commentService.getDiscussPostByComment(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> replyVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost targetDiscussPost = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("targetDiscussPost", targetDiscussPost);
                replyVOList.add(map);
            }
        }

        model.addAttribute("replyVOList", replyVOList);
        model.addAttribute("discussPostCount", discussPostCount);

        // 用户主页中，判断是显示我的帖子还是TA的帖子; 判断是显示我的回复还是TA的回复
        boolean isMyPost = false, isMyReply = false;
        if (hostHolder.getUser() != null) {
            isMyPost = hostHolder.getUser().getId() == userId;
            isMyReply = hostHolder.getUser().getId() == userId;
        }
        model.addAttribute("isMyPost", isMyPost);
        model.addAttribute("isMyReply", isMyReply);

        return "/site/my-reply";
    }
}
