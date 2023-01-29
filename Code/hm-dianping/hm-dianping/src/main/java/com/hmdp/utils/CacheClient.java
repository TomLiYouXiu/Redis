package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author liyouxiu
 * @date 2023/1/29 21:26
 */
@Slf4j
@Component
public class CacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    //将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        //设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData),time,unit);
    }

    //根据指定的key查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
    public <R,ID> R queryWithThrough(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback,Long time, TimeUnit unit){ //设置泛型，并返回值
        //Function<ID,R> dbFallback 传递函数<参数，返回值>  Function有参有返回值
        String key=keyPrefix+id;
        //1.从Redis中查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if(StrUtil.isNotBlank(json)){
            //3.存在直接返回  JSON转换为Java实体
            return JSONUtil.toBean(json, type);
        }
        //判断命中的是否是空值
        if(json!=null){
            //返回错误信息
            return null;
        }
        //4 不存在，根据ID查询数据库
        R r = dbFallback.apply(id);
        //5.不存在，返回错误
        if (r == null) {
            //解决缓存穿透。将空值写入redis
            stringRedisTemplate.opsForValue().set(key,"",time,unit);
            //返回错误信息
            return null;
        }
        //6.存在 写入redis
        this.set(key,r,time,unit);
        //8.返回
        return r;
    }

    //根据指定的key查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
    public <R,ID>R queryWithLogicalExpire(String keyPrefix,ID id,Class<R> type,String lockKeyPrefix,Function<ID,R> dbFallback,Long time, TimeUnit unit){
        String key=keyPrefix+id;
        //1.从Redis中查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if(StrUtil.isBlank(json)){
            //3.不存在
            return null;
        }
        //4.命中，需要先把JSON反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject)redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5.判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            //5.1未过期，直接返回店铺信息
            return r;
        }
        //5.2已过期，需要缓存重建
        //6.缓存重建
        String lockKey=lockKeyPrefix+id;
        //6.1获取互斥锁
        boolean lock = tryLock(lockKey);
        //6.2判断是否获取锁成功
        if(BooleanUtil.isTrue(lock)){
            //6.3成功，开启独立线程实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try {
                    //查询数据库
                    R r1 = dbFallback.apply(id);
                    //写入Redis
                    this.setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    //释放锁
                    unLock(lockKey);
                }
            });
        }
        //6.4返回过期的商铺信息
        return r;
    }
    //尝试获取锁 利用锁解决缓存击穿问题
    private boolean tryLock(String key){
        //setIfAbsent == setnx 不存在则赋值
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        //防止拆箱
        return BooleanUtil.isTrue(flag);
    }


    //删除锁，进行锁的释放
    private  void unLock(String key){
        stringRedisTemplate.delete(key);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
}
