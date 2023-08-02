package io.metersphere.system.job;

import base.BaseTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LicenseCacheTests extends BaseTest {
    @Resource
    private LicenseCacheJob licenseCacheJob;


    @Test
    public void cleanupProject() throws Exception{
        //TODO
        licenseCacheJob.checkLicenseTask();
    }
}
