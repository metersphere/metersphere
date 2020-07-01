package io.metersphere.track.service;


import com.alibaba.excel.EasyExcelFactory;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.listener.EasyExcelListener;
import io.metersphere.excel.listener.TestCaseDataListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.request.testcase.TestCaseBatchRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseService {

    @Resource
    TestCaseMapper testCaseMapper;

    @Resource
    ExtTestCaseMapper extTestCaseMapper;

    @Resource
    TestPlanMapper testPlanMapper;

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;

    @Resource
    ProjectMapper projectMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    TestCaseNodeService testCaseNodeService;

    @Resource
    UserMapper userMapper;

    @Resource
    UserRoleMapper userRoleMapper;

    public void addTestCase(TestCaseWithBLOBs testCase) {
        testCase.setName(testCase.getName());
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria()
                .andProjectIdEqualTo(testCase.getProjectId())
                .andNameEqualTo(testCase.getName());
        List<TestCase> testCases = testCaseMapper.selectByExample(testCaseExample);
        if (testCases.size() > 0) {
            MSException.throwException(Translator.get("test_case_exist") + testCase.getName());
        }
        testCase.setId(UUID.randomUUID().toString());
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        testCaseMapper.insert(testCase);
    }

    public List<TestCase> getTestCaseByNodeId(List<String> nodeIds) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andNodeIdIn(nodeIds);
        return testCaseMapper.selectByExample(testCaseExample);
    }

    public TestCaseWithBLOBs getTestCase(String testCaseId) {
        return testCaseMapper.selectByPrimaryKey(testCaseId);
    }

    public int editTestCase(TestCaseWithBLOBs testCase) {
        testCase.setUpdateTime(System.currentTimeMillis());
        checkTestCaseExist(testCase);
        return testCaseMapper.updateByPrimaryKeySelective(testCase);
    }

    private void checkTestCaseExist (TestCaseWithBLOBs testCase) {
        if (testCase.getName() != null) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria()
                    .andNameEqualTo(testCase.getName())
                    .andProjectIdEqualTo(testCase.getProjectId())
                    .andIdNotEqualTo(testCase.getId());
            if (testCaseMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_already_exists"));
            }
        }
    }

    public int deleteTestCase(String testCaseId) {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andCaseIdEqualTo(testCaseId);
        testPlanTestCaseMapper.deleteByExample(example);
        return testCaseMapper.deleteByPrimaryKey(testCaseId);
    }

    public List<TestCaseDTO> listTestCase(QueryTestCaseRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        // 对模板导入的测试用例排序
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        return extTestCaseMapper.list(request);
    }

    public List<TestCaseDTO> listTestCaseMthod(QueryTestCaseRequest request) {
        return extTestCaseMapper.listByMethod(request);
    }


    /**
     * 获取测试用例
     * 过滤已关联
     *
     * @param request
     * @return
     */
    public List<TestCase> getTestCaseNames(QueryTestCaseRequest request) {
        if (StringUtils.isNotBlank(request.getPlanId())) {
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
            if (testPlan != null) {
                request.setProjectId(testPlan.getProjectId());
            }
        }

        List<TestCase> testCaseNames = extTestCaseMapper.getTestCaseNames(request);

        if ( StringUtils.isNotBlank(request.getPlanId()) ) {
            TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
            testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(request.getPlanId());
            List<String> relevanceIds = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample).stream()
                    .map(TestPlanTestCase::getCaseId)
                    .collect(Collectors.toList());

            return testCaseNames.stream()
                    .filter(testcase -> !relevanceIds.contains(testcase.getId()))
                    .collect(Collectors.toList());
        }

        return testCaseNames;

    }


    public List<TestCase> recentTestPlans(QueryTestCaseRequest request, int count) {

        if (StringUtils.isBlank(request.getWorkspaceId())) {
            return null;
        }

        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(request.getWorkspaceId());

        List<String> projectIds = projectMapper.selectByExample(projectExample).stream()
                .map(Project::getId).collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return null;
        }

        PageHelper.startPage(1, count, true);

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andProjectIdIn(projectIds);
        testCaseExample.setOrderByClause("update_time desc, sort desc");
        return testCaseMapper.selectByExample(testCaseExample);
    }

    public Project getProjectByTestCaseId(String testCaseId) {
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCaseId);
        if (testCaseWithBLOBs == null) {
            return null;
        }
        return projectMapper.selectByPrimaryKey(testCaseWithBLOBs.getProjectId());
    }

    public ExcelResponse testCaseImport(MultipartFile file, String projectId) {

        ExcelResponse excelResponse = new ExcelResponse();

        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestCaseRequest queryTestCaseRequest = new QueryTestCaseRequest();
        queryTestCaseRequest.setProjectId(projectId);
        List<TestCase> testCases = extTestCaseMapper.getTestCaseNames(queryTestCaseRequest);
        Set<String> testCaseNames = testCases.stream()
                .map(TestCase::getName)
                .collect(Collectors.toSet());

        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria()
                .andRoleIdIn(Arrays.asList(RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER))
                .andSourceIdEqualTo(currentWorkspaceId);

        Set<String> userIds = userRoleMapper.selectByExample(userRoleExample).stream().map(UserRole::getUserId).collect(Collectors.toSet());

        EasyExcelListener easyExcelListener = null;
        List<ExcelErrData<TestCaseExcelData>> errList = null;
        try {
            easyExcelListener = new TestCaseDataListener(this, projectId, testCaseNames, userIds);
            EasyExcelFactory.read(file.getInputStream(), TestCaseExcelData.class, easyExcelListener).sheet().doRead();
            errList = easyExcelListener.getErrList();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        } finally {
            easyExcelListener.close();
        }
        //如果包含错误信息就导出错误信息
        if (!errList.isEmpty()) {
            excelResponse.setSuccess(false);
            excelResponse.setErrList(errList);
        } else {
            excelResponse.setSuccess(true);
        }
        return excelResponse;
    }

    public void saveImportData(List<TestCaseWithBLOBs> testCases, String projectId) {

        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        if (!testCases.isEmpty()) {
            AtomicInteger sort = new AtomicInteger();
            testCases.forEach(testcase -> {
                testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                testcase.setSort(sort.getAndIncrement());
                mapper.insert(testcase);
            });
        }
        sqlSession.flushStatements();
    }

    public void testCaseTemplateExport(HttpServletResponse response) {
        EasyExcelExporter easyExcelExporter = null;
        try {
            easyExcelExporter = new EasyExcelExporter(TestCaseExcelData.class);
            easyExcelExporter.export(response, generateExportTemplate(),
                    Translator.get("test_case_import_template_name"), Translator.get("test_case_import_template_sheet"));
        } catch (Exception e) {
            MSException.throwException(e);
        } finally {
            easyExcelExporter.close();
        }
    }

    private List<TestCaseExcelData> generateExportTemplate() {
        List<TestCaseExcelData> list = new ArrayList<>();
        StringBuilder path = new StringBuilder("");
        List<String> types = Arrays.asList("functional", "performance", "api");
        List<String> methods = Arrays.asList("manual", "auto");
        SessionUser user = SessionUtils.getUser();
        for (int i = 1; i <= 5; i++) {
            TestCaseExcelData data = new TestCaseExcelData();
            data.setName(Translator.get("test_case") + i);
            path.append("/" + Translator.get("module") + i);
            data.setNodePath(path.toString());
            data.setPriority("P" + i%4);
            data.setType(types.get(i%3));
            data.setMethod(methods.get(i%2));
            data.setPrerequisite(Translator.get("preconditions_optional"));
            data.setStepDesc("1. " + Translator.get("step_tip_separate") +
                    "\n2. " + Translator.get("step_tip_order") + "\n3. " + Translator.get("step_tip_optional"));
            data.setStepResult("1. " + Translator.get("step_tip_order") + "\n2. " + Translator.get("result_tip_order") + "\n3. " + Translator.get("result_tip_optional"));
            data.setMaintainer(user.getId());
            data.setRemark(Translator.get("remark_optional"));
            list.add(data);
        }

        list.add(new TestCaseExcelData());
        TestCaseExcelData explain = new TestCaseExcelData();
        explain.setName(Translator.get("do_not_modify_header_order"));
        explain.setNodePath(Translator.get("module_created_automatically"));
        explain.setType(Translator.get("options") + "（functional、performance、api）");
        explain.setMethod(Translator.get("options") + "（manual、auto）");
        explain.setPriority(Translator.get("options") + "（P0、P1、P2、P3）");
        explain.setMaintainer(Translator.get("please_input_workspace_member"));

        list.add(explain);
        return list;
    }

    public void editTestCaseBath(TestCaseBatchRequest request) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(request.getIds());

        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        BeanUtils.copyBean(testCase, request);
        testCaseMapper.updateByExampleSelective(
                testCase,
                testCaseExample);

    }

    public void deleteTestCaseBath(TestCaseBatchRequest request) {
        deleteTestPlanTestCaseBath(request.getIds());
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        testCaseMapper.deleteByExample(example);
    }

    public void deleteTestPlanTestCaseBath(List<String> caseIds) {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andCaseIdIn(caseIds);
        testPlanTestCaseMapper.deleteByExample(example);
    }

    public void deleteTestCaseByProjectId(String projectId) {
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        testCaseMapper.deleteByExample(example);
    }
}
