package io.metersphere.bug.config;

import io.metersphere.provider.BaseAssociateCaseProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class BugProviderConfiguration {

    @MockBean
    BaseAssociateCaseProvider baseAssociateCaseProvider;
}
