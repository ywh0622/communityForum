package com.communityforum.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author YWH
 * @Description LogAspect
 * @Date 2023/4/29 15:59
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

    @Pointcut("execution (* com.communityforum.controller.*.*(..))")
    public void controllerPointCut() {
    }

    @Pointcut("execution (* com.communityforum.service.*.*(..))")
    public void servicePointCut() {
    }

    @Before("controllerPointCut()")
    public void beforeController(JoinPoint joinPoint) {
        // 用户[1.2.3.4] 在[xxx],访问了xxx
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // getDeclaringTypeName:类名  getName：方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        log.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }

    @Before("servicePointCut()")
    public void beforeService(JoinPoint joinPoint) {
        // 用户[1.2.3.4] 在[xxx],访问了xxx
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // getDeclaringTypeName:类名  getName：方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        log.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }
}
