package com.hmdp.utils;

import lombok.Data;

import java.time.LocalDateTime;
//解决缓存击穿，设置逻辑过期时间
// 逻辑过期时间
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
