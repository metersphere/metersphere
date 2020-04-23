package io.metersphere.service;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.listener.EasyExcelListener;
import io.metersphere.excel.listener.TestCaseDataListener;
import io.metersphere.excel.utils.EasyExcelUtil;
import io.metersphere.user.SessionUser;
import io.metersphere.user.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
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

    public void addTestCase(TestCaseWithBLOBs testCase) {
        testCase.setId(UUID.randomUUID().toString());
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        testCaseMapper.insert(testCase);
    }

    public List<TestCase> getTestCaseByNodeId(List<Integer> nodeIds) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andNodeIdIn(nodeIds);
        return testCaseMapper.selectByExample(testCaseExample);
    }

    public TestCaseWithBLOBs getTestCase(String testCaseId) {
        return testCaseMapper.selectByPrimaryKey(testCaseId);
    }

    public int editTestCase(TestCaseWithBLOBs testCase) {
        testCase.setUpdateTime(System.currentTimeMillis());
        return testCaseMapper.updateByPrimaryKeySelective(testCase);
    }

    public int deleteTestCase(String testCaseId) {
        return testCaseMapper.deleteByPrimaryKey(testCaseId);
    }

    public List<TestCaseWithBLOBs> listTestCase(QueryTestCaseRequest request) {
        TestCaseExample testCaseExample = new TestCaseExample();
        TestCaseExample.Criteria criteria = testCaseExample.createCriteria();
        if ( StringUtils.isNotBlank(request.getName()) ) {
            criteria.andNameLike("%" + request.getName() + "%");
        }
        if ( StringUtils.isNotBlank(request.getProjectId()) ) {
            criteria.andProjectIdEqualTo(request.getProjectId());
        }
        if ( request.getNodeIds() != null && request.getNodeIds().size() > 0) {
            criteria.andNodeIdIn(request.getNodeIds());
        }
        return testCaseMapper.selectByExampleWithBLOBs(testCaseExample);
    }

    /**
     * 获取测试用例
     * 过滤已关联
     * @param request
     * @return
     */
    public List<TestCase> getTestCaseNames(QueryTestCaseRequest request) {
        if ( StringUtils.isNotBlank(request.getPlanId()) ) {
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
            request.setProjectId(testPlan.getProjectId());
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
        testCaseExample.setOrderByClause("update_time desc");
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

        try {

            ExcelResponse excelResponse = new ExcelResponse();

            String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
            QueryTestCaseRequest queryTestCaseRequest = new QueryTestCaseRequest();
            queryTestCaseRequest.setProjectId(projectId);
            List<TestCase> testCases = extTestCaseMapper.getTestCaseNames(queryTestCaseRequest);
            Set<String> testCaseNames = testCases.stream()
                    .map(TestCase::getName)
                    .collect(Collectors.toSet());

            UserExample userExample =  new UserExample();
            userExample.createCriteria().andLastWorkspaceIdEqualTo(currentWorkspaceId);
            List<User> users = userMapper.selectByExample(userExample);
            Set<String> userIds = users.stream().map(User::getId).collect(Collectors.toSet());

            EasyExcelListener easyExcelListener = new TestCaseDataListener(this, projectId,
                    testCaseNames, userIds, TestCaseExcelData.class);
            EasyExcelFactory.read(file.getInputStream(), TestCaseExcelData.class, easyExcelListener).sheet().doRead();

            List<ExcelErrData<TestCaseExcelData>> errList = easyExcelListener.getErrList();
            //如果包含错误信息就导出错误信息
            if (!errList.isEmpty()) {
                excelResponse.setSuccess(false);
                excelResponse.setErrList(errList);
            } else {
                excelResponse.setSuccess(true);
            }
            return excelResponse;

        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    public void saveImportData(List<TestCaseWithBLOBs> testCases, String projectId) {

        Map<String, Integer> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        if (!testCases.isEmpty()) {
            testCases.forEach(testcase -> {
                testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                mapper.insert(testcase);
            });
        }
        sqlSession.flushStatements();
    }

    public void testCaseTemplateExport(HttpServletResponse response) {
       EasyExcelUtil.export(response, TestCaseExcelData.class, generateExportTemplate(), "测试用例模版", "模版");
    }

    private List<TestCaseExcelData> generateExportTemplate() {
        List<TestCaseExcelData> list = new ArrayList<>();
        StringBuilder path = new StringBuilder("");
        List<String> types = Arrays.asList("functional", "performance", "api");
        List<String> methods = Arrays.asList("manual", "auto");
        SessionUser user = SessionUtils.getUser();
        for (int i = 1; i <= 5; i++) {
            TestCaseExcelData data = new TestCaseExcelData();
            data.setName("测试用例" + i);
            path.append("/" + "模块" + i);
            data.setNodePath(path.toString());
            data.setPriority("P" + i%4);
            data.setType(types.get(i%3));
            data.setMethod(methods.get(i%2));
            data.setPrerequisite("前置条件选填");
            data.setStepDesc("1. 每个步骤以换行分隔\n2. 步骤前可标序号\n3. 测试步骤和结果选填");
            data.setStepResult("1. 每条结果以换行分隔\n2. 结果前可标序号\n3. 测试步骤和结果选填");
            data.setMaintainer(user.getId());
            data.setRemark("备注选填");
            list.add(data);
        }

        list.add(new TestCaseExcelData());
        TestCaseExcelData explain = new TestCaseExcelData();
        explain.setName("同一项目下测试用例名称不能重复！");
        explain.setNodePath("模块名称请按照'/模块1/模块2'的格式书写; 错误格式示例:('/', '/tes//test'); 若无该模块，则自动创建模块");
        explain.setType("用例类型必须为：functional、performance、api");
        explain.setMethod("测试方式必须为：manual、auto");
        explain.setPriority("优先级必须为：P0、P1、P2、P3");
        explain.setMaintainer("维护人必须为该工作空间相关人员");

        list.add(explain);
        return list;
    }
}
