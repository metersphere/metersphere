package io.metersphere.bug.config;

import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.system.service.LicenseService;
import org.springframework.boot.test.context.TestConfiguration; 
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
public class BugProviderConfiguration {

    @MockitoBean
    BaseAssociateCaseProvider baseAssociateCaseProvider;

    @MockitoBean
    LicenseService licenseService;

    @MockitoBean
    Platform platform;
}
