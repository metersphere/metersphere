package io.metersphere.api.exec.scenario;

import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TestPlanResourceService {
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private TestPlanApiCaseMapper tstPlanApiCaseMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePlanScenario(TestPlanApiScenario testPlanApiScenario){
        testPlanApiScenarioMapper.updateByPrimaryKeySelective(testPlanApiScenario);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePlanCase(TestPlanApiCase testPlanApiCase){
        tstPlanApiCaseMapper.updateByPrimaryKeySelective(testPlanApiCase);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateScenario(ApiScenario apiScenario){
        apiScenarioMapper.updateByPrimaryKey(apiScenario);
    }

}
