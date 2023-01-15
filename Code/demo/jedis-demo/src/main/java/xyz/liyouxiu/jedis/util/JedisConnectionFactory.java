package xyz.liyouxiu.jedis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author liyouxiu
 * @date 2023/1/10 22:22
 */
public class JedisConnectionFactory {
    private static final JedisPool jedisPool;

    static{
        //配置连接池
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接数
        jedisPoolConfig.setMaxTotal(8);
        //最大空闲连接
        jedisPoolConfig.setMaxIdle(8);
        //最小空闲连接
        jedisPoolConfig.setMinIdle(0);
        //等待连接时长
        jedisPoolConfig.setMaxWaitMillis(1000);
        //创建连接池对象
        jedisPool=new JedisPool(jedisPoolConfig,
                "192.168.188.137",
                6379,
                1000,
                "123456");

    }
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }
}
