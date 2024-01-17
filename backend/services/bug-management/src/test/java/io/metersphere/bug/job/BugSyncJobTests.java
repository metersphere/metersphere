package io.metersphere.bug.job;

import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.dto.sdk.LicenseInfoDTO;
import io.metersphere.system.service.LicenseService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugSyncJobTests {

    @Resource
    private LicenseService licenseService;

    @Test
    void test() {
        // set licenseService field to null by reflection
        BugSyncJob noLicenseMockObj = new BugSyncJob();
        ReflectionTestUtils.setField(noLicenseMockObj, "licenseService", null);
        noLicenseMockObj.businessExecute(null);
        // set mocLicenseService field
        BugSyncJob syncJob = new BugSyncJob();
        // mock license validate return null
        Mockito.when(licenseService.validate()).thenReturn(null);
        syncJob.businessExecute(null);
        // mock license validate return empty info
        Mockito.when(licenseService.validate()).thenReturn(new LicenseDTO());
        syncJob.businessExecute(null);
        // mock license validate return invalid && license info
        LicenseDTO invalid = new LicenseDTO();
        invalid.setStatus("invalid");
        invalid.setLicense(new LicenseInfoDTO());
        Mockito.when(licenseService.validate()).thenReturn(invalid);
        syncJob.businessExecute(null);
        // mock license validate return valid && license info
        LicenseDTO valid = new LicenseDTO();
        valid.setStatus("valid");
        valid.setLicense(new LicenseInfoDTO());
        Mockito.when(licenseService.validate()).thenReturn(valid);
        syncJob.businessExecute(null);
    }
}
