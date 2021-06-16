package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.service.ApiDefinitionExecResultService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService {

    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    ApiTestCaseService apiTestCaseService;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Lazy
    @Resource
    ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    ApiTestCaseMapper apiTestCaseMapper;

    public TestPlanApiCase getInfo(String caseId, String testPlanId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andApiCaseIdEqualTo(caseId).andTestPlanIdEqualTo(testPlanId);
        return testPlanApiCaseMapper.selectByExample(example).get(0);
    }

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

    public List<String> selectIds(ApiTestCaseRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<String> idList = extTestPlanApiCaseMapper.selectIds(request);
        return idList;
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
        List<String> deleteIds = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            deleteIds = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                deleteIds.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        apiDefinitionExecResultService.deleteByResourceIds(deleteIds);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andIdIn(deleteIds)
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

    public void setExecResult(String id, String status, Long time) {
        TestPlanApiCase apiCase = new TestPlanApiCase();
        apiCase.setId(id);
        apiCase.setStatus(status);
        apiCase.setUpdateTime(time);
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
        request.setIds(new ArrayList<>(ids));
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

    public String getState(String id) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andApiCaseIdEqualTo(id);
        return testPlanApiCaseMapper.selectByExample(example).get(0).getStatus();

    }

    public List<TestPlanApiCaseDTO> selectAllTableRows(TestPlanApiCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        ApiTestCaseRequest selectReq = new ApiTestCaseRequest();
        selectReq.setIds(ids);
        List<TestPlanApiCaseDTO> returnList = extTestPlanApiCaseMapper.list(selectReq);
        return returnList;
    }

    public String getLogDetails(String id) {
        TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(id);
        if (testPlanApiCase != null) {
            ApiTestCaseWithBLOBs testCase = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanApiCase.getTestPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testCase.getProjectId(), testCase.getName(), testPlanApiCase.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanApiCase> nodes = testPlanApiCaseMapper.selectByExample(example);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(nodes)) {
            ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
            testCaseExample.createCriteria().andIdIn(nodes.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList()));
            List<ApiTestCase> testCases = apiTestCaseMapper.selectByExample(testCaseExample);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(testCases)) {
                List<String> names = testCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testCases.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public Boolean hasFailCase(String planId, List<String> apiCaseIds) {
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return false;
        }
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andApiCaseIdIn(apiCaseIds)
                .andStatusEqualTo("error");
        return testPlanApiCaseMapper.countByExample(example) > 0 ? true : false;
    }
}
