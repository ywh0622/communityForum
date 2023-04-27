package com.communityforum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author YWH
 * @Description 自定义注解，方法上面加上该注解后，需要进行后续的判断
 * @Date 2023/4/27 16:06
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
