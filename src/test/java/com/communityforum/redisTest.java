package com.communityforum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.concurrent.TimeUnit;

/**
 * @Author YWH
 * @Description redisTest
 * @Date 2023/4/29 17:29
 */
@SpringBootTest
public class redisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testString() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    void testHash() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "name", "张三");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "name"));

    }

    @Test
    void testList() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 1);
        redisTemplate.opsForList().leftPush(redisKey, 2);
        redisTemplate.opsForList().leftPush(redisKey, 3);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));

    }

    @Test
    void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "张三", "李四", "王五", "马六");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    @Test
    void testSortSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "刘备", 80);
        redisTemplate.opsForZSet().add(redisKey, "张飞", 90);
        redisTemplate.opsForZSet().add(redisKey, "关羽", 100);
        redisTemplate.opsForZSet().add(redisKey, "曹操", 70);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "关羽"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "关羽"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    @Test
    void testKeys() {
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    // 编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = " test:tx";

                operations.multi();
                operations.opsForSet().add(redisKey, "张三", "李四", "tom");
                System.out.println(operations.opsForSet().members(redisKey));
                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}
