package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author liyouxiu
 * @date 2023/1/31 19:03
 * ID 生成器
 */
@Component
public class RedisIdWorker {
    //初始时间
    private static final long BEGIN_TIMESTAMP = 1672531200L;
    private static final int COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String KeyPerfix){
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;
        //2.生成序列号
        //2.1获取当天日期,精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr" + KeyPerfix + ":" + date);
        //3.拼接返回
        return timeStamp << COUNT_BITS | count;
    }

    public static void main(String[] args) {
        //设置起始日期
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        //转化为s 设置时区
        long l = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println(l);
        //l=1672531200
    }
}
