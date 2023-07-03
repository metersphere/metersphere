package io.metersphere.system.job;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

@Component
public class CleanProjectJobTests {
    @Test
    public void cleanupProject() {
        CleanProjectJob cleanProjectJob = new CleanProjectJob();
        //TODO
        cleanProjectJob.cleanupProject();
    }
}
