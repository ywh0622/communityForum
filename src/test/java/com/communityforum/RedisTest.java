package com.communityforum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
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
public class RedisTest {

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

    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog() {
        String redisKey = "test:hyper:01";

        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    // 将三组数据合并，再统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hyper:02";
        String redisKey3 = "test:hyper:03";
        String redisKey4 = "test:hyper:04";
        String redisUnionKey = "test:hyper:union";

        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        redisTemplate.opsForHyperLogLog().union(redisUnionKey, redisKey2, redisKey3, redisKey4);
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisUnionKey));
    }

    // 统计一组数据的bool值
    @Test
    public void testBitMap() {
        String redisKey = "test:bitmap:01";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 3, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }

    // 记录三组数据的布尔值，并对三组数据进行or运算
    @Test
    public void testBitMapOperation() {
        String redisKey2 = "test:bitmap:02";
        String redisKey3 = "test:bitmap:03";
        String redisKey4 = "test:bitmap:04";
        String rediskey = "test:bitmap:or";

        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, rediskey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(rediskey.getBytes());
            }
        });
        System.out.println(obj);
        System.out.println(redisTemplate.opsForValue().getBit(rediskey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey, 6));
    }
}
