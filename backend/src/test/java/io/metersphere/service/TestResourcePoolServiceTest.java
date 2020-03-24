package io.metersphere.service;

import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.TestResourcePoolDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestResourcePoolServiceTest {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @Test
    public void listResourcePools() {
        List<TestResourcePoolDTO> list = testResourcePoolService.listResourcePools(new QueryResourcePoolRequest());
        System.out.println(list.size());
    }
}