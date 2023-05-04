package com.communityforum.config;

import com.communityforum.util.CommunityConstant;
import com.communityforum.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author YWH
 * @Description SpringSecurity相关配置
 * @Date 2023/5/4 13:01
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/user/changePassword",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR
                )
                .anyRequest().permitAll()
                // 关闭CSRF校验
                .and().csrf().disable();
        // 权限不够时
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登陆
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String header = request.getHeader("x-requested-with");
                        // 如果是异步请求，则防护字符串，如果是普通请求，返回页面
                        if ("XMLHttpRequest".equals(header)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登陆!"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 登陆后，权限不足
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String header = request.getHeader("x-requested-with");
                        // 如果是异步请求，则防护字符串，如果是普通请求，返回页面
                        if ("XMLHttpRequest".equals(header)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        // springsecurity底层默认会拦截/logout请求，进行退出处理，
        // 覆盖它的底层逻辑，才能执行我们自己的退出逻辑
        http.logout().logoutUrl("/securitylogout");// 给了一个项目中不存在的路径
    }

}
