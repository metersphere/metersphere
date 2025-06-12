package io.metersphere.plan.config;

import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.provider.BaseAssociateScenarioProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
public class CaseTestConfiguration {

    @MockitoBean
    BaseAssociateApiProvider provider;

    @MockitoBean
    BaseAssociateScenarioProvider scenarioProvider;

    @MockitoBean
    BaseAssociateBugProvider baseAssociateBugProvider;

}
