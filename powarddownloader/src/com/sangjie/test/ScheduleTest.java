package com.sangjie.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledExecutorService
 */
public class ScheduleTest {
    public static void main(String[] args) {
        //获取对象
        ScheduledExecutorService s =  Executors.newScheduledThreadPool(1);

        //延时两秒后执行，每间隔三秒执行任务
        s.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis());
                //模拟耗时操作
                try {
                    TimeUnit.SECONDS.sleep(6);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 2, 3, TimeUnit.SECONDS);
    }

    public static void schedule() {
        //获取对象
        ScheduledExecutorService s =  Executors.newScheduledThreadPool(1);

        //延时两秒之后在执行任务
        s.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
            }
        }, 2, TimeUnit.SECONDS);

        //关闭
        s.shutdown();
    }
}
