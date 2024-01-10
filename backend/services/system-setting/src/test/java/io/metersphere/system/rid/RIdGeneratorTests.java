package io.metersphere.system.rid;

import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.uid.NumGenerator;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RIdGeneratorTests {

    @Test
    @Order(1)
    public void testId1() throws Exception {
        String projectId = "100001";

        long capacity = 10; // 容量，代表每个项目最多可以生成多少个id
        long init = 100001L; // 代表从1000001开始，项目的 num
        long start = System.currentTimeMillis();
        AtomicLong atomicLong = new AtomicLong(init);
        // 使用多线程执行
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < capacity; i++) {
            executorService.submit(() -> {
                long nextId = NumGenerator.nextNum(projectId, ApplicationNumScope.API_DEFINITION);
                System.out.println(Thread.currentThread() + " -> " + nextId);
                synchronized (projectId) {
                    if (atomicLong.get() < nextId) {
                        atomicLong.set(nextId);
                    }
                }
            });
        }
        executorService.close();
        System.out.println("耗时: " + (System.currentTimeMillis() - start) + "ms");
        Assertions.assertEquals(capacity + init, atomicLong.get() + 1);
    }

    @Test
    @Order(2)
    public void testId2() throws Exception {
        String projectId = "100001";

        long capacity = 2000; // 容量，代表每个项目最多可以生成多少个id
        long init = 1; // 代表从1000001开始，项目的 num
        long apiNum = 100005;
        long start = System.currentTimeMillis();
        AtomicLong atomicLong = new AtomicLong(init);
        // 使用多线程执行
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < capacity; i++) {
            executorService.submit(() -> {
                // 接口用例的前缀为: PROJECT_ID_API_DEFINITION 比较特殊
                long nextId = NumGenerator.nextNum(projectId + "_" + apiNum, ApplicationNumScope.API_TEST_CASE);
                System.out.println(Thread.currentThread() + " -> " + nextId);
                synchronized (projectId) {
                    if (atomicLong.get() < nextId) {
                        atomicLong.set(nextId);
                    }
                }
            });
        }
        executorService.close();
        System.out.println("耗时: " + (System.currentTimeMillis() - start) + "ms");
        Assertions.assertEquals(Long.parseLong("" + apiNum + capacity), atomicLong.get());
    }

    @Test
    @Order(3)
    public void testClean() {
        NumGenerator numGenerator = CommonBeanFactory.getBean(NumGenerator.class);
        numGenerator.cleanDeletedProjectResource();
    }
}
