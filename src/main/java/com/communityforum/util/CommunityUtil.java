package com.communityforum.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @Author YWH
 * @Description 工具类
 * @Date 2023/4/26 14:50
 */
public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // MD5加密
    // hello -> xxxxxxx
    // hello + salt(随机字符串) -> xxxxxxuuxxxix
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
