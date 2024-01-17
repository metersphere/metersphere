package io.metersphere.functional.service;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.constants.AssociateCaseType;
import io.metersphere.functional.domain.FunctionalCaseTest;
import io.metersphere.functional.domain.FunctionalCaseTestExample;
import io.metersphere.functional.dto.FunctionalCaseTestDTO;
import io.metersphere.functional.dto.FunctionalCaseTestPlanDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseModuleMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseTestMapper;
import io.metersphere.functional.mapper.FunctionalCaseTestMapper;
import io.metersphere.functional.request.AssociatePlanPageRequest;
import io.metersphere.functional.request.DisassociateOtherCaseRequest;
import io.metersphere.functional.request.FunctionalCaseTestRequest;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.request.*;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.redisson.api.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guoyuqi
 * 功能用例关联其他用例服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalTestCaseService {

    @Resource
    private BaseAssociateApiProvider provider;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    private ExtFunctionalCaseTestMapper extFunctionalCaseTestMapper;

    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;

    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;

    @Resource
    private ExtFunctionalCaseModuleMapper extFunctionalCaseModuleMapper;

    private static final String UNPLANNED_API = "api_unplanned_request";

    @Resource
    private BaseAssociateBugProvider baseAssociateBugProvider;


    /**
     * 获取功能用例未关联的接口用例列表
     *
     * @param request request
     * @return List<ApiTestCaseProviderDTO>
     */
    public List<TestCaseProviderDTO> page(TestCasePageProviderRequest request) {
        return provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
    }

    /**
     * 根据接口用例的搜索条件获取符合条件的接口定义的模块统计数量
     *
     * @param request 接口用例高级搜索条件
     * @param deleted 接口定义是否删除
     * @return 接口模块统计数量
     */
    public Map<String, Long> moduleCount(TestCasePageProviderRequest request, boolean deleted) {
        return provider.moduleCount("functional_case_test", "case_id", "source_id", request, deleted);

    }

    /**
     * 关联其他用例
     *
     * @param request request
     * @param deleted 接口定义是否删除
     */
    public void associateCase(AssociateOtherCaseRequest request, boolean deleted, String userId) {

        switch (request.getSourceType()) {
            case AssociateCaseType.API -> associateApi(request, deleted, userId);
            case AssociateCaseType.SCENARIO -> associateScenario(request, deleted, userId);
            default -> LogUtils.info("AssociateCaseType: " + request.getSourceType());
        }


    }

    private void associateScenario(AssociateOtherCaseRequest request, boolean deleted, String userId) {
        LogUtils.info("关联场景");
    }

    private void associateApi(AssociateOtherCaseRequest request, boolean deleted, String userId) {
        List<ApiTestCase> apiTestCases = provider.getSelectApiTestCases(request, deleted);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseTestMapper caseTestMapper = sqlSession.getMapper(FunctionalCaseTestMapper.class);
        for (ApiTestCase apiTestCase : apiTestCases) {
            FunctionalCaseTest functionalCaseTest = new FunctionalCaseTest();
            functionalCaseTest.setCaseId(request.getSourceId());
            functionalCaseTest.setProjectId(request.getProjectId());
            functionalCaseTest.setSourceId(apiTestCase.getId());
            functionalCaseTest.setVersionId(apiTestCase.getVersionId());
            functionalCaseTest.setSourceType(request.getSourceType());
            functionalCaseTest.setId(IdGenerator.random().generateId());
            functionalCaseTest.setCreateUser(userId);
            functionalCaseTest.setCreateTime(System.currentTimeMillis());
            functionalCaseTest.setUpdateUser(userId);
            functionalCaseTest.setUpdateTime(System.currentTimeMillis());
            caseTestMapper.insert(functionalCaseTest);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    /**
     * 取消关联其他用例
     *
     * @param request request
     */
    public void disassociateCase(DisassociateOtherCaseRequest request) {
        List<String> functionalTestCaseIds = doSelectIds(request);
        FunctionalCaseTestExample functionalCaseTestExample = new FunctionalCaseTestExample();
        if (CollectionUtils.isNotEmpty(functionalTestCaseIds)) {
            functionalCaseTestExample.createCriteria().andIdIn(functionalTestCaseIds);
            functionalCaseTestMapper.deleteByExample(functionalCaseTestExample);
        }
    }


    public List<String> doSelectIds(DisassociateOtherCaseRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extFunctionalCaseTestMapper.getIds(request);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public List<BaseTreeNode> getTree(AssociateCaseModuleRequest request) {
        List<BaseTreeNode> fileModuleList = new ArrayList<>();
        switch (request.getSourceType()) {
            case AssociateCaseType.API -> fileModuleList = extFunctionalCaseModuleMapper.selectApiCaseModuleByRequest(request);
            case AssociateCaseType.SCENARIO -> fileModuleList = extFunctionalCaseModuleMapper.selectApiScenarioModuleByRequest(request);
            default -> new ArrayList<>();
        }
        return functionalCaseModuleService.buildTreeAndCountResource(fileModuleList, true, Translator.get(UNPLANNED_API));
    }

    public List<FunctionalCaseTestDTO> hasAssociatePage(FunctionalCaseTestRequest request) {
        return extFunctionalCaseTestMapper.getList(request);
    }


    /**
     * 获取功能用例未关联的缺陷列表
     *
     * @param request request
     * @return
     */
    public List<BugProviderDTO> bugPage(BugPageProviderRequest request) {
        return baseAssociateBugProvider.getBugList("bug_relation_case", "case_id", "bug_id", request);
    }


    /**
     * 关联缺陷
     *
     * @param request request
     * @param deleted 缺陷是否删除
     * @param userId  用户id
     */
    public void associateBug(AssociateBugRequest request, boolean deleted, String userId) {
        List<String> ids = baseAssociateBugProvider.getSelectBugs(request, deleted);
        if (CollectionUtils.isNotEmpty(ids)) {
            baseAssociateBugProvider.handleAssociateBug(ids, userId, request.getCaseId());
        }
    }

    public void disassociateBug(String id) {
        baseAssociateBugProvider.disassociateBug(id);
    }

    public List<BugProviderDTO> hasAssociateBugPage(AssociateBugPageRequest request) {
        return baseAssociateBugProvider.hasAssociateBugPage(request);
    }

    /**
     * 查询已关联的测试计划列表
     * @param request request
     * @return List<FunctionalCaseTestPlanDTO>
     */
    public List<FunctionalCaseTestPlanDTO> hasAssociatePlanPage(AssociatePlanPageRequest request) {
        return extFunctionalCaseTestMapper.getPlanList(request);
    }
}
