package io.metersphere.functional.config;

import io.metersphere.provider.BaseAssociateApiProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class CaseTestConfiguration {

    @MockBean
    BaseAssociateApiProvider provider;

}
