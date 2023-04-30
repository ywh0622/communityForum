package com.communityforum.controller;

import com.communityforum.annotation.LoginRequired;
import com.communityforum.entity.User;
import com.communityforum.service.UserService;
import com.communityforum.util.CommunityConstant;
import com.communityforum.util.CommunityUtil;
import com.communityforum.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author YWH
 * @Description 登陆注册
 * @Date 2023/4/26 14:39
 */
@Controller
@Slf4j(topic = "LoginController")
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @GetMapping("/forget")
    public String getForgetPage() {
        return "/site/forget";
    }

    /**
     * 注册账号
     *
     * @param model
     * @param user
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，已发送激活邮件...");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活账号
     *
     * @param model
     * @param userId
     * @param code
     * @return
     */
    // http://localhost:80/communityForum/activation/101/code
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已被激活!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，激活码错误!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 生成验证码，将生成的验证码放入session中，key为kaptcha,value为四位数的值，并将图片通过response传回给浏览器
     *
     * @param response
     * @param
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/**, HttpSession session **/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        // session.setAttribute("kaptcha", text);

        // 验证码的归属用户
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            log.error("响应验证码失败:" + e.getMessage());
        }
    }

    /**
     * 登陆账号
     *
     * @param username
     * @param password
     * @param code
     * @param rememberme
     * @param model
     * @param
     * @param response
     * @return
     */
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /**HttpSession session,**/HttpServletResponse response,
                        @CookieValue(value = "kaptchaOwner", required = false) String kaptchaOwner) {
        // 检查验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码错误");
            return "/site/login";
        }

        // 检查账号、密码
        long expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge((int) expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 退出登陆
     *
     * @param ticket
     * @return
     */
    @GetMapping("/logout")
    @LoginRequired
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/index";
    }

    /**
     * 获取验证码
     *
     * @param email
     * @param
     * @return
     */
    @GetMapping("/forget/code")
    @ResponseBody
    public String getForgetCode(String email,HttpServletResponse response/**, HttpSession session**/) {
        //先验证邮箱是否为空
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }

        Map<String, Object> map = userService.verifyEmail(email);
        // 如果返回值map中有verifyCode字段，则表示验证邮箱成功且已经向目标邮箱发送验证码
        if (map.containsKey("verifyCode")) {
//            session.setAttribute("verifyCode", map.get("verifyCode"));
            // 验证码的归属用户
            String verifyCodeOwner = CommunityUtil.generateUUID();
            Cookie cookie = new Cookie("verifyCodeOwner",verifyCodeOwner);
            cookie.setMaxAge(60);
            cookie.setPath(contextPath);
            response.addCookie(cookie);
            // 将验证码存入redis
            String redisKey = RedisKeyUtil.getForgetKaptchaKey(verifyCodeOwner);
            redisTemplate.opsForValue().set(redisKey,map.get("verifyCode"),300,TimeUnit.SECONDS);
            return CommunityUtil.getJSONString(0);
        } else {
            return CommunityUtil.getJSONString(1, "查询不到该邮箱注册信息!");
        }
    }

    /**
     * 重置密码
     *
     * @param email
     * @param password
     * @param verifyCode
     * @param model
     * @param
     * @return
     */
    @PostMapping("/forget/password")
    public String forgetPassword(String email, String password, String verifyCode, Model model/**, HttpSession session**/,
                                 @CookieValue(value = "verifyCodeOwner",required = false) String  verifyCodeOwner) {
        try {
//            String code = session.getAttribute("verifyCode").toString();
            String code = null;
            if (StringUtils.isNotBlank(verifyCodeOwner)){
                String redisKey = RedisKeyUtil.getForgetKaptchaKey(verifyCodeOwner);
                code = (String) redisTemplate.opsForValue().get(redisKey);
            }
            // 判断验证码是否一致
            if (StringUtils.isBlank(verifyCode.trim()) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode.trim())) {
                model.addAttribute("codeMsg", "验证码错误!");
                return "/site/forget";
            }

            Map<String, Object> map = userService.resetPassword(email, password);

            if (map.containsKey("user")) {
                model.addAttribute("msg", "修改密码成功，请重新登录");
                model.addAttribute("target", "/login");
                return "/site/operate-result";
            } else {
                model.addAttribute("emailMsg", map.get("emailMsg"));
                model.addAttribute("passwordMsg", map.get("passwordMsg"));
                return "/site/forget";
            }
        } catch (Exception e) {
            model.addAttribute("codeMsg", "验证码失效，请重新获取验证码!");
            return "/site/forget";
        }
    }
}
