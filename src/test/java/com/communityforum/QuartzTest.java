package com.communityforum;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author YWH
 * @Description QuartzTest
 * @Date 2023/5/5 15:18
 */
@SpringBootTest
public class QuartzTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDeleteJob(){
        try {
            boolean result = scheduler.deleteJob(new JobKey("testJob", "testJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
