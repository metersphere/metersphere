package io.metersphere.functional.config;

import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.provider.BaseAssociateScenarioProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class CaseTestConfiguration {

    @MockBean
    BaseAssociateApiProvider provider;

    @MockBean
    BaseAssociateScenarioProvider scenarioProvider;

    @MockBean
    BaseAssociateBugProvider baseAssociateBugProvider;

}
