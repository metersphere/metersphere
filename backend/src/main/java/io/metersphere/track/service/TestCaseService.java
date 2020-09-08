package io.metersphere.track.service;


import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.constants.TestCaseConstants;
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
import org.springframework.util.CollectionUtils;
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

    @Resource
    TestCaseIssueService testCaseIssueService;

    public void addTestCase(TestCaseWithBLOBs testCase) {
        testCase.setName(testCase.getName());
        checkTestCaseExist(testCase);
        testCase.setId(UUID.randomUUID().toString());
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        testCase.setNum(getNextNum(testCase.getProjectId()));
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

    private void checkTestCaseExist(TestCaseWithBLOBs testCase) {

        // 全部字段值相同才判断为用例存在
        if (testCase != null) {
            TestCaseExample example = new TestCaseExample();
            TestCaseExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(testCase.getName())
                    .andProjectIdEqualTo(testCase.getProjectId())
                    .andNodePathEqualTo(testCase.getNodePath())
                    .andTypeEqualTo(testCase.getType())
                    .andMaintainerEqualTo(testCase.getMaintainer())
                    .andPriorityEqualTo(testCase.getPriority())
                    .andMethodEqualTo(testCase.getMethod());

//            if (StringUtils.isNotBlank(testCase.getNodeId())) {
//                criteria.andNodeIdEqualTo(testCase.getTestId());
//            }

            if (StringUtils.isNotBlank(testCase.getTestId())) {
                criteria.andTestIdEqualTo(testCase.getTestId());
            }

            if (StringUtils.isNotBlank(testCase.getPrerequisite())) {
                criteria.andPrerequisiteEqualTo(testCase.getPrerequisite());
            }

            if (StringUtils.isNotBlank(testCase.getId())) {
                criteria.andIdNotEqualTo(testCase.getId());
            }

            List<TestCaseWithBLOBs> caseList = testCaseMapper.selectByExampleWithBLOBs(example);

            // 如果上边字段全部相同，去检查 steps 和 remark
            if (!CollectionUtils.isEmpty(caseList)) {
                caseList.forEach(tc -> {
                    String steps = tc.getSteps();
                    String remark = tc.getRemark();
                    if (StringUtils.equals(steps, testCase.getSteps()) && StringUtils.equals(remark, testCase.getRemark())) {
                        MSException.throwException(Translator.get("test_case_already_exists"));
                    }
                });
            }

        }
    }

    public int deleteTestCase(String testCaseId) {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andCaseIdEqualTo(testCaseId);
        testPlanTestCaseMapper.deleteByExample(example);
        testCaseIssueService.delTestCaseIssues(testCaseId);
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
            // request 传入要查询的 projectId 切换的项目ID
        }

        List<TestCase> testCaseNames = extTestCaseMapper.getTestCaseNames(request);

        if (StringUtils.isNotBlank(request.getPlanId())) {
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
            AtomicInteger num = new AtomicInteger();
            num.set(getNextNum(projectId) + testCases.size());
            testCases.forEach(testcase -> {
                testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                testcase.setSort(sort.getAndIncrement());
                testcase.setNum(num.decrementAndGet());
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
        List<String> types = TestCaseConstants.Type.getValues();
        List<String> methods = TestCaseConstants.Method.getValues();
        SessionUser user = SessionUtils.getUser();
        for (int i = 1; i <= 5; i++) {
            TestCaseExcelData data = new TestCaseExcelData();
            data.setName(Translator.get("test_case") + i);
            path.append("/" + Translator.get("module") + i);
            data.setNodePath(path.toString());
            data.setPriority("P" + i % 4);
            String type = types.get(i % 3);
            data.setType(type);
            if (StringUtils.equals(TestCaseConstants.Type.Functional.getValue(), type)) {
                data.setMethod(TestCaseConstants.Method.Manual.getValue());
            } else {
                data.setMethod(methods.get(i % 2));
            }
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

    public void testCaseExport(HttpServletResponse response, TestCaseBatchRequest request) {
        EasyExcelExporter easyExcelExporter = null;
        try {
            easyExcelExporter = new EasyExcelExporter(TestCaseExcelData.class);
            easyExcelExporter.export(response, generateTestCaseExcel(request),
                    Translator.get("test_case_import_template_name"), Translator.get("test_case_import_template_sheet"));
        } catch (Exception e) {
            MSException.throwException(e);
        } finally {
            easyExcelExporter.close();
        }
    }

    private List<TestCaseExcelData> generateTestCaseExcel(TestCaseBatchRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        List<TestCaseDTO> TestCaseList = extTestCaseMapper.listByTestCaseIds(request);
        List<TestCaseExcelData> list = new ArrayList<>();
        StringBuilder step = new StringBuilder("");
        StringBuilder result = new StringBuilder("");
        TestCaseList.forEach(t -> {
            TestCaseExcelData data = new TestCaseExcelData();
            data.setName(t.getName());
            data.setNodePath(t.getNodePath());
            data.setPriority(t.getPriority());
            data.setType(t.getType());
            data.setMethod(t.getMethod());
            data.setPrerequisite(t.getPrerequisite());
            if (t.getMethod().equals("manual")) {
                String steps = t.getSteps();
                JSONArray jsonArray = JSON.parseArray(steps);
                for (int j = 0; j < jsonArray.size(); j++) {
                    int num = j + 1;
                    step.append(num + "." + jsonArray.getJSONObject(j).getString("desc") + "\n");
                    result.append(num + "." + jsonArray.getJSONObject(j).getString("result") + "\n");

                }
                data.setStepDesc(step.toString());
                data.setStepResult(result.toString());
                step.setLength(0);
                result.setLength(0);
                data.setRemark(t.getRemark());

            } else if (t.getMethod().equals("auto") && t.getType().equals("api")) {
                data.setStepDesc("");
                data.setStepResult("");
                if(t.getTestId().equals("other")){
                    data.setRemark(t.getOtherTestName());
                }else{
                    data.setRemark(t.getApiName());
                }

            } else if (t.getMethod().equals("auto") && t.getType().equals("performance")) {
                data.setStepDesc("");
                data.setStepResult("");
                if(t.getTestId().equals("other")){
                    data.setRemark(t.getOtherTestName());
                }else{
                    data.setRemark(t.getPerformName());
                }
            }
            data.setMaintainer(t.getMaintainer());
            list.add(data);
        });
        return list;
    }


    public void editTestCaseBath(TestCaseBatchRequest request) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(request.getIds());

        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        BeanUtils.copyBean(testCase, request);
        testCase.setUpdateTime(System.currentTimeMillis());
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

    /**
     * 是否关联测试
     *
     * @param testId
     */
    public void checkIsRelateTest(String testId) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andTestIdEqualTo(testId);
        List<TestCase> testCases = testCaseMapper.selectByExample(testCaseExample);
        StringBuilder caseName = new StringBuilder();
        if (testCases.size() > 0) {
            for (TestCase testCase : testCases) {
                caseName = caseName.append(testCase.getName()).append(",");
            }
            String str = caseName.toString().substring(0, caseName.length() - 1);
            MSException.throwException(Translator.get("related_case_del_fail_prefix") + " " + str + " " + Translator.get("related_case_del_fail_suffix"));
        }
    }

    /**
     * 获取项目下一个num (页面展示的ID)
     *
     * @return
     */
    private int getNextNum(String projectId) {
        TestCase testCase = extTestCaseMapper.getMaxNumByProjectId(projectId);
        if (testCase == null) {
            return 100001;
        } else {
            return Optional.ofNullable(testCase.getNum() + 1).orElse(100001);
        }
    }


    /**
     * 导入用例前，检查数据库是否存在此用例
     *
     * @param testCaseWithBLOBs
     * @return
     */
    public boolean exist(TestCaseWithBLOBs testCaseWithBLOBs) {

        try {
            checkTestCaseExist(testCaseWithBLOBs);
        } catch (MSException e) {
            return true;
        }

        return false;
    }
}
