package io.metersphere.track.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.service.ApiDefinitionExecResultService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.ApiTestCaseExample;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiCaseExample;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import org.apache.jmeter.testelement.TestElement;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

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
    ApiDefinitionService apiDefinitionService;
    @Lazy
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

    public List<String> getExecResultByPlanId(String plan) {
        return extTestPlanApiCaseMapper.getExecResultByPlanId(plan);
    }

    public Pager<List<ApiTestCaseDTO>> relevanceList(int goPage, int pageSize, ApiTestCaseRequest request) {
        List<String> ids = apiTestCaseService.selectIdsNotExistsInPlan(request.getProjectId(), request.getPlanId());
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        if (CollectionUtils.isEmpty(ids)) {
            return PageUtils.setPageInfo(page, new ArrayList<>());
        }
        request.setIds(ids);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiTestCaseService.listSimple(request));
    }

    public int delete(String id) {
        apiDefinitionExecResultService.deleteByResourceId(id);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andIdEqualTo(id);

        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public int deleteByPlanId(String planId) {
        List<String> ids = extTestPlanApiCaseMapper.getIdsByPlanId(planId);
        apiDefinitionExecResultService.deleteByResourceIds(ids);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        apiDefinitionExecResultService.deleteByResourceIds(request.getIds());
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andIdIn(request.getIds())
                .andTestPlanIdEqualTo(request.getPlanId());
        testPlanApiCaseMapper.deleteByExample(example);
    }

    public List<TestPlanApiCase> getCasesByPlanId(String planId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.selectByExample(example);
    }

    public TestPlanApiCase getById(String id) {
        return testPlanApiCaseMapper.selectByPrimaryKey(id);
    }

    public void setExecResult(String id, String status) {
        TestPlanApiCase apiCase = new TestPlanApiCase();
        apiCase.setId(id);
        apiCase.setStatus(status);
        apiCase.setUpdateTime(System.currentTimeMillis());
        testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
    }

    public void updateByPrimaryKeySelective(TestPlanApiCase apiCase) {
        testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
    }

    public void deleteByRelevanceProjectIds(String planId, List<String> relevanceProjectIds) {
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
        request.setPlanId(planId);
        request.setIds(extTestPlanApiCaseMapper.getNotRelevanceCaseIds(planId, relevanceProjectIds));
        deleteApiCaseBath(request);
    }

    public void batchUpdateEnv(TestPlanApiCaseBatchRequest request) {
        // 批量修改用例环境
        Map<String, String> rows = request.getSelectRows();
        Set<String> ids = rows.keySet();
        Map<String, String> env = request.getProjectEnvMap();
        if (env != null && !env.isEmpty()) {
            ids.forEach(id -> {
                TestPlanApiCase apiCase = new TestPlanApiCase();
                apiCase.setId(id);
                apiCase.setEnvironmentId(env.get(rows.get(id)));
                testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
            });
        }
    }
}
