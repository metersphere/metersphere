package io.metersphere.system.job;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CleanUserInviteJobTests {
    @Resource
    private CleanUserInviteJob cleanUserInviteJob;

    @Test
    public void cleanupProject() {
        cleanUserInviteJob.cleanUserInvite();
    }
}
