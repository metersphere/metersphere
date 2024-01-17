package io.metersphere.bug.config;

import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.system.service.LicenseService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class BugProviderConfiguration {

    @MockBean
    BaseAssociateCaseProvider baseAssociateCaseProvider;

    @MockBean
    LicenseService licenseService;

    @MockBean
    Platform platform;
}
