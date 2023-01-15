package xyz.liyouxiu.springjedisdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.liyouxiu.springjedisdemo.pojo.User;

import javax.jws.soap.SOAPBinding;
import java.util.Map;


@SpringBootTest
class RedisStringTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();
    @Test
    void testString() {
        //写入String数据
        redisTemplate.opsForValue().set("name","虎哥");
        //获取数据
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println("name="+name);
    }
    @Test
    void testSaveUser() throws JsonProcessingException {
        //创建对象
        User user = new User("刀哥", 21);
        //手动序列化
        String json = mapper.writeValueAsString(user);
        //写入数据
        redisTemplate.opsForValue().set("user:200",json);

        //获取数据
        String jsonUser = redisTemplate.opsForValue().get("user:200");
        //手动序列化
        User user1 = mapper.readValue(jsonUser, User.class);
        System.out.println("user1="+user1);
    }
    @Test
    void testHash(){
        //测试hash，存数据
        redisTemplate.opsForHash().put("user:300","name","jack");
        redisTemplate.opsForHash().put("user:300","age","22");

        //取数据
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("user:300");
        System.out.println(entries);
    }
}
