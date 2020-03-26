package io.metersphere.service;

import io.metersphere.base.domain.TestCaseNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestCaseTest {


    @Resource
    TestCaseNodeService testCaseNodeService;

    @Test
    public void addNode() {

        TestCaseNode node = new TestCaseNode();
        node.setName("node01");
        node.setProjectId("2ade216b-01a6-43d0-b48c-4a3898306096");
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        testCaseNodeService.addNode(node);

    }

}
