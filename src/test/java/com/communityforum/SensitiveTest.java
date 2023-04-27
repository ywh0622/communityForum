package com.communityforum;

import com.communityforum.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author YWH
 * @Description 敏感词测试
 * @Date 2023/4/27 18:06
 */
@SpringBootTest
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    void testSensitiveFilter() {
        String text = "○这里可以○吸○毒、赌博、○嫖○娼○、111good,fabc,☆f☆a☆b☆c☆";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
