package xyz.liyouxiu.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import xyz.liyouxiu.jedis.util.JedisConnectionFactory;

import java.util.Map;

/**
 * @author liyouxiu
 * @date 2023/1/9 16:02
 */
public class JedisTest {
    //1.引入依赖

    //2.连接
    private Jedis jedis;

    @BeforeEach
    void setUp() {
        //2.1建立连接
//        jedis=new Jedis("192.168.188.137",6379);
        jedis= JedisConnectionFactory.getJedis();
        //2.2设置密码
        jedis.auth("123456");
        //2.3选择库
        jedis.select(0);
    }

    //

    @Test
    void testString() {
        //3.1操作Redis
        String result = jedis.set("name", "优秀");
        System.out.println("result="+result);
        //3.2获取数据
        String name = jedis.get("name");
        System.out.println("name="+name);
    }
    @Test
    void testHash(){
        //插入hash数据
        jedis.hset("user:1","name","jack");
        jedis.hset("user:1","age","21");

        //获取
        Map<String, String> map = jedis.hgetAll("user:1");
        System.out.println(map);
    }

    @AfterEach
    void tearDown() {
        //4.释放连接
         if(jedis!=null){
             jedis.close();
         }
    }
}
