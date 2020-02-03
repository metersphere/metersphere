package io.metersphere;


import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
    @Resource
    UserMapper userMapper;

    @Test
    public void test1() {
        List<User> users = userMapper.selectByExample(null);
        System.out.println(users);
    }
}
