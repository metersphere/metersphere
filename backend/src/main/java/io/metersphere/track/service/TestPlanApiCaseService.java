package io.metersphere.track.service;

import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.service.ApiDefinitionExecResultService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.TestPlanApiCaseExample;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService {

    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    ApiTestCaseService apiTestCaseService;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    ApiDefinitionExecResultService apiDefinitionExecResultService;

    public List<TestPlanApiCaseDTO> list(ApiTestCaseRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<TestPlanApiCaseDTO> apiTestCases = extTestPlanApiCaseMapper.list(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        apiTestCaseService.buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public List<ApiTestCaseDTO> relevanceList(ApiTestCaseRequest request) {
        List<String> ids = apiTestCaseService.selectIdsNotExistsInPlan(request.getProjectId(), request.getPlanId());
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        request.setIds(ids);
        return apiTestCaseService.listSimple(request);
    }

    public int delete(String planId, String id) {
        apiDefinitionExecResultService.deleteByResourceId(id);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andIdEqualTo(id);

        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
        apiDefinitionExecResultService.deleteByResourceIds(request.getIds());
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andIdIn(request.getIds())
                .andTestPlanIdEqualTo(request.getPlanId());
        testPlanApiCaseMapper.deleteByExample(example);
    }
}
