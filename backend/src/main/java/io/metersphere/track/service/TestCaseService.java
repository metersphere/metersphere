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
import io.metersphere.commons.constants.TestCaseReviewStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.domain.TestCaseExcelDataFactory;
import io.metersphere.excel.listener.TestCaseDataListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.service.FileService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.EditTestCaseRequest;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.request.testcase.TestCaseBatchRequest;
import io.metersphere.track.request.testcase.TestCaseMinderEditRequest;
import io.metersphere.xmind.XmindCaseParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseService {
    @Resource
    TestCaseNodeMapper testCaseNodeMapper;

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

    @Lazy
    @Resource
    ProjectService projectService;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    TestCaseNodeService testCaseNodeService;

    @Resource
    UserRoleMapper userRoleMapper;

    @Resource
    TestCaseIssueService testCaseIssueService;
    @Resource
    TestCaseReviewTestCaseMapper testCaseReviewTestCaseMapper;
    @Resource
    TestCaseCommentService testCaseCommentService;
    @Resource
    FileService fileService;
    @Resource
    TestCaseFileMapper testCaseFileMapper;
    @Resource
    TestCaseTestMapper testCaseTestMapper;
    private void setNode(TestCaseWithBLOBs testCase){
        if (StringUtils.isEmpty(testCase.getNodeId()) || "default-module".equals(testCase.getNodeId())) {
            TestCaseNodeExample example = new TestCaseNodeExample();
            example.createCriteria().andProjectIdEqualTo(testCase.getProjectId()).andNameEqualTo("默认模块");
            List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(nodes)) {
                testCase.setNodeId(nodes.get(0).getId());
                testCase.setNodePath("/" + nodes.get(0).getName());
            }
        }
    }

    public TestCaseWithBLOBs addTestCase(TestCaseWithBLOBs testCase) {
        testCase.setName(testCase.getName());
        checkTestCaseExist(testCase);
        testCase.setId(UUID.randomUUID().toString());
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        checkTestCustomNum(testCase);
        testCase.setNum(getNextNum(testCase.getProjectId()));
        testCase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
        testCase.setDemandId(testCase.getDemandId());
        testCase.setDemandName(testCase.getDemandName());
        this.setNode(testCase);
        testCaseMapper.insert(testCase);
        return testCase;
    }

    private void checkTestCustomNum(TestCaseWithBLOBs testCase) {
        if (StringUtils.isNotBlank(testCase.getCustomNum())) {
            String projectId = testCase.getProjectId();
            Project project = projectService.getProjectById(projectId);
            if (project != null) {
                Boolean customNum = project.getCustomNum();
                // 未开启自定义ID
                if (!customNum) {
                    testCase.setCustomNum(null);
                } else {
                    checkCustomNumExist(testCase);
                }
            } else {
                MSException.throwException("add test case fail, project is not find.");
            }
        }
    }

    private void checkCustomNumExist(TestCaseWithBLOBs testCase) {
        TestCaseExample example = new TestCaseExample();
        example.createCriteria()
                .andCustomNumEqualTo(testCase.getCustomNum())
                .andProjectIdEqualTo(testCase.getProjectId())
                .andIdNotEqualTo(testCase.getId());
        List<TestCase> list = testCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            MSException.throwException(Translator.get("custom_num_is_exist"));
        }
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
        checkTestCustomNum(testCase);
        testCase.setUpdateTime(System.currentTimeMillis());
        return testCaseMapper.updateByPrimaryKeySelective(testCase);
    }

    public TestCaseWithBLOBs checkTestCaseExist(TestCaseWithBLOBs testCase) {

        // 全部字段值相同才判断为用例存在
        if (testCase != null) {

            /*
            例如对于“/模块5”，用户的输入可能为“模块5”或者“/模块5/”或者“模块5/”。
            不这样处理的话，下面进行判断时就会用用户输入的错误格式进行判断，而模块名为“/模块5”、
            “模块5”、“/模块5/”、“模块5/”时，它们应该被认为是同一个模块。
            数据库存储的node_path都是“/模块5”这种格式的
             */
            String nodePath = testCase.getNodePath();
            if (!nodePath.startsWith("/")) {
                nodePath = "/" + nodePath;
            }
            if (nodePath.endsWith("/")) {
                nodePath = nodePath.substring(0, nodePath.length() - 1);
            }

            TestCaseExample example = new TestCaseExample();
            TestCaseExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(testCase.getName())
                    .andProjectIdEqualTo(testCase.getProjectId())
                    .andNodePathEqualTo(nodePath)
                    .andTypeEqualTo(testCase.getType())
                    .andMaintainerEqualTo(testCase.getMaintainer())
                    .andPriorityEqualTo(testCase.getPriority());
//                    .andMethodEqualTo(testCase.getMethod());

//            if (StringUtils.isNotBlank(testCase.getNodeId())) {
//                criteria.andNodeIdEqualTo(testCase.getTestId());
//            }

            if (StringUtils.isNotBlank(testCase.getTestId())) {
                criteria.andTestIdEqualTo(testCase.getTestId());
            }

            if (StringUtils.isNotBlank(testCase.getId())) {
                criteria.andIdNotEqualTo(testCase.getId());
            }

            List<TestCaseWithBLOBs> caseList = testCaseMapper.selectByExampleWithBLOBs(example);

            // 如果上边字段全部相同，去检查 remark 和 steps
            if (!CollectionUtils.isEmpty(caseList)) {
                String caseRemark = testCase.getRemark();
                String caseSteps = testCase.getSteps();
                String casePrerequisite = testCase.getPrerequisite();
                for (TestCaseWithBLOBs tc : caseList) {
                    String steps = tc.getSteps();
                    String remark = tc.getRemark();
                    String prerequisite = tc.getPrerequisite();
                    if (StringUtils.equals(steps, caseSteps) && StringUtils.equals(remark, caseRemark) && StringUtils.equals(prerequisite, casePrerequisite)) {
                         //MSException.throwException(Translator.get("test_case_already_exists"));
                        return tc;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据id和pojectId查询id是否在数据库中存在。
     * 在数据库中单id的话是可重复的,id与projectId的组合是唯一的
     */
    public Integer checkIdExist(Integer id, String projectId) {
        TestCaseExample example = new TestCaseExample();
        TestCaseExample.Criteria criteria = example.createCriteria();
        if (null != id) {
            criteria.andNumEqualTo(id);
            criteria.andProjectIdEqualTo(projectId);
            long count = testCaseMapper.countByExample(example);    //查询是否有包含此ID的数据
            if (count == 0) {  //如果ID不存在
                return null;
            } else { //有对应ID的数据
                return id;
            }
        }
        return null;
    }

    public int deleteTestCase(String testCaseId) {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andCaseIdEqualTo(testCaseId);
        testPlanTestCaseMapper.deleteByExample(example);
        testCaseIssueService.delTestCaseIssues(testCaseId);
        testCaseCommentService.deleteCaseComment(testCaseId);
        TestCaseTestExample examples = new TestCaseTestExample();
        examples.createCriteria().andTestCaseIdEqualTo(testCaseId);
        testCaseTestMapper.deleteByExample(examples);
        return testCaseMapper.deleteByPrimaryKey(testCaseId);
    }

    public List<TestCaseDTO> listTestCase(QueryTestCaseRequest request) {
        this.initRequest(request, true);
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        // 对模板导入的测试用例排序
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        return extTestCaseMapper.list(request);
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param checkThisWeekData
     * @return
     */
    private void initRequest(QueryTestCaseRequest request, boolean checkThisWeekData) {
        if (checkThisWeekData) {
            Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
            Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
            if (request.isSelectThisWeedData()) {
                if (weekFirstTime != null) {
                    request.setCreateTime(weekFirstTime.getTime());
                }
            }
            if (request.isSelectThisWeedRelevanceData()) {
                if (weekFirstTime != null) {
                    request.setRelevanceCreateTime(weekFirstTime.getTime());
                }
            }

        }
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
    public List<TestCase> getTestCaseRelateList(QueryTestCaseRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        return getTestCaseByNotInPlan(request);
    }

    public List<TestCase> getTestCaseByNotInPlan(QueryTestCaseRequest request) {
        return extTestCaseMapper.getTestCaseByNotInPlan(request);
    }

    public List<TestCaseDTO> getTestCaseByNotInIssue(QueryTestCaseRequest request) {
        List<TestCaseDTO> list = extTestCaseMapper.getTestCaseByNotInIssue(request);
        addProjectName(list);
        return list;
    }

    public void addProjectName(List<TestCaseDTO> list) {
        List<String> projectIds = list.stream()
                .map(TestCase::getProjectId)
                .collect(Collectors.toList());
        List<Project> projects = projectService.getProjectByIds(projectIds);
        Map<String, String> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getId, Project::getName));
        list.forEach(item -> {
            String projectName = projectMap.get(item.getProjectId());
            if (StringUtils.isNotBlank(projectName)) {
                item.setProjectName(projectName);
            }
        });
    }

    public List<TestCase> getReviewCase(QueryTestCaseRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        // 对模板导入的测试用例排序
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        return extTestCaseMapper.getTestCaseByNotInReview(request);
    }


    public List<TestCase> recentTestPlans(QueryTestCaseRequest request, int count) {
        PageHelper.startPage(1, count, true);
        TestCaseExample testCaseExample = new TestCaseExample();
        TestCaseExample.Criteria criteria = testCaseExample.createCriteria();
        criteria.andMaintainerEqualTo(request.getUserId());
        String projectId = SessionUtils.getCurrentProjectId();
        if (StringUtils.isNotBlank(projectId)) {
            criteria.andProjectIdEqualTo(projectId);
            testCaseExample.setOrderByClause("update_time desc, sort desc");
            return testCaseMapper.selectByExample(testCaseExample);
        }
        return new ArrayList<>();
    }

    public Project getProjectByTestCaseId(String testCaseId) {
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCaseId);
        if (testCaseWithBLOBs == null) {
            return null;
        }
        return projectMapper.selectByPrimaryKey(testCaseWithBLOBs.getProjectId());
    }


    public ExcelResponse testCaseImport(MultipartFile multipartFile, String projectId, String userId) {

        ExcelResponse excelResponse = new ExcelResponse();
        boolean isUpdated = false;  //判断是否更新了用例
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestCaseRequest queryTestCaseRequest = new QueryTestCaseRequest();
        queryTestCaseRequest.setProjectId(projectId);
        List<TestCase> testCases = extTestCaseMapper.getTestCaseNames(queryTestCaseRequest);
        Set<String> testCaseNames = testCases.stream()
                .map(TestCase::getName)
                .collect(Collectors.toSet());
        List<ExcelErrData<TestCaseExcelData>> errList = null;
        if (multipartFile == null) {
            MSException.throwException(Translator.get("upload_fail"));
        }
        if (multipartFile.getOriginalFilename().endsWith(".xmind")) {
            try {
                XmindCaseParser xmindParser = new XmindCaseParser(this, userId, projectId, testCaseNames);
                errList = xmindParser.parse(multipartFile);
                if (CollectionUtils.isEmpty(xmindParser.getNodePaths())
                        && CollectionUtils.isEmpty(xmindParser.getTestCase())
                        && CollectionUtils.isEmpty(xmindParser.getUpdateTestCase())) {
                    if (errList == null) {
                        errList = new ArrayList<>();
                    }
                    ExcelErrData excelErrData = new ExcelErrData(null, 1, Translator.get("upload_fail") + "：" + Translator.get("upload_content_is_null"));
                    errList.add(excelErrData);
                    excelResponse.setErrList(errList);
                }
                if (errList.isEmpty()) {
                    if (CollectionUtils.isNotEmpty(xmindParser.getNodePaths())) {
                        testCaseNodeService.createNodes(xmindParser.getNodePaths(), projectId);
                    }
                    if (CollectionUtils.isNotEmpty(xmindParser.getTestCase())) {
                        Collections.reverse(xmindParser.getTestCase());
                        this.saveImportData(xmindParser.getTestCase(), projectId);
                    }
                    if (CollectionUtils.isNotEmpty(xmindParser.getUpdateTestCase())) {
                        this.updateImportData(xmindParser.getUpdateTestCase(), projectId);
                    }
                }
                xmindParser.clear();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(e.getMessage());
            }

        } else {
            UserRoleExample userRoleExample = new UserRoleExample();
            userRoleExample.createCriteria()
                    .andRoleIdIn(Arrays.asList(RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER))
                    .andSourceIdEqualTo(currentWorkspaceId);

            Set<String> userIds = userRoleMapper.selectByExample(userRoleExample).stream().map(UserRole::getUserId).collect(Collectors.toSet());

            try {
                //根据本地语言环境选择用哪种数据对象进行存放读取的数据
                Class clazz = new TestCaseExcelDataFactory().getExcelDataByLocal();

                TestCaseDataListener easyExcelListener = new TestCaseDataListener(clazz, projectId, testCaseNames, userIds);
                //读取excel数据
                EasyExcelFactory.read(multipartFile.getInputStream(), clazz, easyExcelListener).sheet().doRead();

                errList = easyExcelListener.getErrList();
                isUpdated = easyExcelListener.isUpdated();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(e.getMessage());
            }

        }
        //如果包含错误信息就导出错误信息
        if (!errList.isEmpty()) {
            excelResponse.setSuccess(false);
            excelResponse.setErrList(errList);
            excelResponse.setIsUpdated(isUpdated);
        } else {
            excelResponse.setSuccess(true);
        }
        return excelResponse;
    }

    public void saveImportData(List<TestCaseWithBLOBs> testCases, String projectId) {
        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        Project project = projectService.getProjectById(projectId);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        if (!testCases.isEmpty()) {
            AtomicInteger sort = new AtomicInteger();
            AtomicInteger num = new AtomicInteger();
            num.set(getNextNum(projectId) + testCases.size());
            testCases.forEach(testcase -> {
                testcase.setId(UUID.randomUUID().toString());
                testcase.setCreateTime(System.currentTimeMillis());
                testcase.setUpdateTime(System.currentTimeMillis());
                testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                testcase.setSort(sort.getAndIncrement());
                int number = num.decrementAndGet();
                testcase.setNum(number);
                if (project.getCustomNum() && StringUtils.isBlank(testcase.getCustomNum())) {
                    testcase.setCustomNum(String.valueOf(number));
                }
                testcase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
                    mapper.insert(testcase);
            });
        }
        sqlSession.flushStatements();
    }

    public void updateImportData(List<TestCaseWithBLOBs> testCases, String projectId) {
        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        if (!testCases.isEmpty()) {
            AtomicInteger sort = new AtomicInteger();
            AtomicInteger num = new AtomicInteger();
            num.set(getNextNum(projectId) + testCases.size());
            testCases.forEach(testcase -> {
                testcase.setUpdateTime(System.currentTimeMillis());
                testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                testcase.setSort(sort.getAndIncrement());
                testcase.setNum(num.decrementAndGet());
                testcase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
                mapper.updateByPrimaryKeySelective(testcase);
            });
        }
        sqlSession.flushStatements();
    }

    /**
     * 把Excel中带ID的数据更新到数据库
     * feat(测试跟踪):通过Excel导入导出时有ID字段，可通过Excel导入来更新用例。 (#1727)
     * @param testCases
     * @param projectId
     */
    public void updateImportDataCarryId(List<TestCaseWithBLOBs> testCases, String projectId) {
        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);

        /*
        获取用例的“网页上所显示id”与“数据库ID”映射。
         */
        List<Integer> nums = testCases.stream()
                .map(TestCase::getNum)
                .collect(Collectors.toList());
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andNumIn(nums)
                .andProjectIdEqualTo(projectId);
        List<TestCase> testCasesList = testCaseMapper.selectByExample(example);
        Map<Integer, String> numIdMap = testCasesList.stream()
                .collect(Collectors.toMap(TestCase::getNum, TestCase::getId));


        if (!testCases.isEmpty()) {
            AtomicInteger sort = new AtomicInteger();
            testCases.forEach(testcase -> {
                testcase.setUpdateTime(System.currentTimeMillis());
                testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                testcase.setSort(sort.getAndIncrement());
                testcase.setId(numIdMap.get(testcase.getNum()));
                mapper.updateByPrimaryKeySelective(testcase);
            });
        }
        sqlSession.flushStatements();
    }


    public void testCaseTemplateExport(HttpServletResponse response) {
        try {
            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(new TestCaseExcelDataFactory().getExcelDataByLocal());
            easyExcelExporter.export(response, generateExportTemplate(),
                    Translator.get("test_case_import_template_name"), Translator.get("test_case_import_template_sheet"));
        } catch (Exception e) {
            MSException.throwException(e);
        }
    }

    public void download(HttpServletResponse res) throws IOException {
        // 发送给客户端的数据
        byte[] buff = new byte[1024];
        try (OutputStream outputStream = res.getOutputStream();
             BufferedInputStream bis = new BufferedInputStream(TestCaseService.class.getResourceAsStream("/io/metersphere/xmind/template/xmind.xml"));) {
            int i = bis.read(buff);
            while (i != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                i = bis.read(buff);
            }
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException("下载思维导图模版失败");
        }
    }

    public void testCaseXmindTemplateExport(HttpServletResponse response) {
        try {
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("思维导图用例模版", "UTF-8") + ".xmind");
            download(response);
        } catch (Exception ex) {

        }
    }

    private List<TestCaseExcelData> generateExportTemplate() {
        List<TestCaseExcelData> list = new ArrayList<>();
        StringBuilder path = new StringBuilder("");
        List<String> types = TestCaseConstants.Type.getValues();
//        List<String> methods = TestCaseConstants.Method.getValues();
        SessionUser user = SessionUtils.getUser();
        for (int i = 1; i <= 5; i++) {
            TestCaseExcelData data = new TestCaseExcelData();
            data.setName(Translator.get("test_case") + i);
            path.append("/" + Translator.get("module") + i);
            data.setNodePath(path.toString());
            data.setPriority("P" + i % 4);
            String type = types.get(i % 3);
            data.setType(type);
//            if (StringUtils.equals(TestCaseConstants.Type.Functional.getValue(), type)) {
//                data.setMethod(TestCaseConstants.Method.Manual.getValue());
//            } else {
//                data.setMethod(methods.get(i % 2));
//            }
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
        explain.setName(Translator.get("do_not_modify_header_order") + "," + Translator.get("num_needed_modify_testcase") + "," + Translator.get("num_needless_create_testcase"));
        explain.setNodePath(Translator.get("module_created_automatically"));
        explain.setType(Translator.get("options") + "（functional、performance、api）");
        explain.setTags(Translator.get("tag_tip_pattern"));
//        explain.setMethod(Translator.get("options") + "（manual、auto）");
        explain.setPriority(Translator.get("options") + "（P0、P1、P2、P3）");
        explain.setMaintainer(Translator.get("please_input_workspace_member"));

        list.add(explain);
        return list;
    }

    public void testCaseExport(HttpServletResponse response, TestCaseBatchRequest request) {
        try {
            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(new TestCaseExcelDataFactory().getExcelDataByLocal());
            easyExcelExporter.export(response, generateTestCaseExcel(request),
                    Translator.get("test_case_import_template_name"), Translator.get("test_case_import_template_sheet"));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }
    }

    private List<TestCaseExcelData> generateTestCaseExcel(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        QueryTestCaseRequest condition = request.getCondition();
        List<OrderRequest> orderList = new ArrayList<>();
        if (condition != null) {
            orderList = ServiceUtils.getDefaultOrder(condition.getOrders());
        }
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
            data.setNum(t.getNum());
            data.setName(t.getName());
            data.setNodePath(t.getNodePath());
            data.setPriority(t.getPriority());
            data.setType(t.getType());
            data.setCustomNum(t.getCustomNum());
            if (StringUtils.isBlank(t.getStepModel())) {
                data.setStepModel(TestCaseConstants.StepModel.STEP.name());
            } else {
                data.setStepModel(t.getStepModel());
            }
//            data.setMethod(t.getMethod());
            data.setPrerequisite(t.getPrerequisite());
            data.setTags(t.getTags());
            if (StringUtils.equals(t.getMethod(), "manual") || StringUtils.isBlank(t.getMethod())) {

                if (StringUtils.equals(data.getStepModel(), TestCaseConstants.StepModel.TEXT.name())) {
                    data.setStepDesc(t.getStepDescription());
                    data.setStepResult(t.getExpectedResult());
                } else {
                    String steps = t.getSteps();
                    String setp = "";
                    setp = steps;
                    JSONArray jsonArray = null;

                    //解决旧版本保存用例导出报错
                    try {
                        jsonArray = JSON.parseArray(setp);
                    } catch (Exception e) {
                        if (steps.contains("null") && !steps.contains("\"null\"")) {
                            setp = steps.replace("null", "\"\"");
                            jsonArray = JSON.parseArray(setp);
                        }
                    }

                    if (CollectionUtils.isNotEmpty(jsonArray)) {
                        for (int j = 0; j < jsonArray.size(); j++) {
                            int num = j + 1;
                            step.append(num + "." + jsonArray.getJSONObject(j).getString("desc") + "\r\n");
                            result.append(num + "." + jsonArray.getJSONObject(j).getString("result") + "\r\n");

                        }
                    }

                    data.setStepDesc(step.toString());
                    data.setStepResult(result.toString());
                    step.setLength(0);
                    result.setLength(0);
                }
                data.setRemark(t.getRemark());

            } else if ("auto".equals(t.getMethod()) && "api".equals(t.getType())) {
                data.setStepDesc("");
                data.setStepResult("");
                if (t.getTestId() != null && "other".equals(t.getTestId())) {
                    data.setRemark(t.getOtherTestName());
                } else {
                    data.setRemark("[" + t.getApiName() + "]" + "\n" + t.getRemark());
                }

            } else if ("auto".equals(t.getMethod()) && "performance".equals(t.getType())) {
                data.setStepDesc("");
                data.setStepResult("");
                if (t.getTestId() != null && "other".equals(t.getTestId())) {
                    data.setRemark(t.getOtherTestName());
                } else {
                    data.setRemark(t.getPerformName());
                }
            }
            data.setMaintainer(t.getMaintainer());
            list.add(data);
        });
        return list;
    }


    public void editTestCaseBath(TestCaseBatchRequest request) {
        TestCaseExample example = this.getBatchExample(request);
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        BeanUtils.copyBean(testCase, request);
        testCase.setUpdateTime(System.currentTimeMillis());
        testCaseMapper.updateByExampleSelective(testCase, example);
    }

    public void deleteTestCaseBath(TestCaseBatchRequest request) {
        TestCaseExample example = this.getBatchExample(request);
        deleteTestPlanTestCaseBath(request.getIds());
        testCaseMapper.deleteByExample(example);
    }

    public TestCaseExample getBatchExample(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        return example;
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
            String str = caseName.substring(0, caseName.length() - 1);
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
            TestCaseWithBLOBs caseWithBLOBs = checkTestCaseExist(testCaseWithBLOBs);
            if (caseWithBLOBs != null)
                return true;
        } catch (MSException e) {
            return true;
        }

        return false;
    }

    public String save(EditTestCaseRequest request, List<MultipartFile> files) {
        if (files == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }

        final TestCaseWithBLOBs testCaseWithBLOBs = addTestCase(request);
        //插入测试与用例关系表
        if (!CollectionUtils.isEmpty(request.getSelected())) {
            List<List<String>> selecteds = request.getSelected();
            TestCaseTest test = new TestCaseTest();
            selecteds.forEach(id -> {
                test.setTestType(id.get(0));
                test.setTestId(id.get(1));
                test.setTestCaseId(request.getId());
                test.setCreateTime(System.currentTimeMillis());
                test.setUpdateTime(System.currentTimeMillis());
                testCaseTestMapper.insert(test);
            });
        }
        // 复制用例时传入文件ID进行复制
        if (!CollectionUtils.isEmpty(request.getFileIds())) {
            List<String> fileIds = request.getFileIds();
            fileIds.forEach(id -> {
                FileMetadata fileMetadata = fileService.copyFile(id);
                TestCaseFile testCaseFile = new TestCaseFile();
                testCaseFile.setCaseId(testCaseWithBLOBs.getId());
                testCaseFile.setFileId(fileMetadata.getId());
                testCaseFileMapper.insert(testCaseFile);
            });
        }


        files.forEach(file -> {
            final FileMetadata fileMetadata = fileService.saveFile(file, testCaseWithBLOBs.getProjectId());
            TestCaseFile testCaseFile = new TestCaseFile();
            testCaseFile.setCaseId(testCaseWithBLOBs.getId());
            testCaseFile.setFileId(fileMetadata.getId());
            testCaseFileMapper.insert(testCaseFile);
        });
        return testCaseWithBLOBs.getId();
    }

    public String edit(EditTestCaseRequest request, List<MultipartFile> files) {
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(request.getId());
        if (testCaseWithBLOBs == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + request.getId());
        }
        //插入测试与用例关系表
        if (!CollectionUtils.isEmpty(request.getSelected())) {
            TestCaseTestExample example = new TestCaseTestExample();
            example.createCriteria().andTestCaseIdEqualTo(request.getId());
            testCaseTestMapper.deleteByExample(example);
            List<List<String>> selecteds = request.getSelected();
            TestCaseTest test = new TestCaseTest();
            selecteds.forEach(id -> {
                test.setTestType(id.get(0));
                test.setTestId(id.get(1));
                test.setCreateTime(System.currentTimeMillis());
                test.setUpdateTime(System.currentTimeMillis());
                test.setTestCaseId(request.getId());
                testCaseTestMapper.insert(test);
            });
        }
        // 新选择了一个文件，删除原来的文件
        List<FileMetadata> updatedFiles = request.getUpdatedFileList();
        List<FileMetadata> originFiles = fileService.getFileMetadataByCaseId(request.getId());
        List<String> updatedFileIds = updatedFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        // 相减
        List<String> deleteFileIds = ListUtils.subtract(originFileIds, updatedFileIds);
        fileService.deleteFileRelatedByIds(deleteFileIds);

        if (!CollectionUtils.isEmpty(deleteFileIds)) {
            TestCaseFileExample testCaseFileExample = new TestCaseFileExample();
            testCaseFileExample.createCriteria().andFileIdIn(deleteFileIds);
            testCaseFileMapper.deleteByExample(testCaseFileExample);
        }


        if (files != null) {
            files.forEach(file -> {
                final FileMetadata fileMetadata = fileService.saveFile(file, testCaseWithBLOBs.getProjectId());
                TestCaseFile testCaseFile = new TestCaseFile();
                testCaseFile.setFileId(fileMetadata.getId());
                testCaseFile.setCaseId(request.getId());
                testCaseFileMapper.insert(testCaseFile);
            });
        }
        this.setNode(request);
        editTestCase(request);
        return request.getId();
    }

    public String editTestCase(EditTestCaseRequest request, List<MultipartFile> files) {
        String testCaseId = testPlanTestCaseMapper.selectByPrimaryKey(request.getId()).getCaseId();
        request.setId(testCaseId);
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCaseId);
        if (testCaseWithBLOBs == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + request.getId());
        }
        testCaseWithBLOBs.setRemark(request.getRemark());
        // 新选择了一个文件，删除原来的文件
        List<FileMetadata> updatedFiles = request.getUpdatedFileList();
        List<FileMetadata> originFiles = fileService.getFileMetadataByCaseId(testCaseId);
        List<String> updatedFileIds = updatedFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        // 相减
        List<String> deleteFileIds = ListUtils.subtract(originFileIds, updatedFileIds);
        fileService.deleteFileRelatedByIds(deleteFileIds);

        if (!CollectionUtils.isEmpty(deleteFileIds)) {
            TestCaseFileExample testCaseFileExample = new TestCaseFileExample();
            testCaseFileExample.createCriteria().andFileIdIn(deleteFileIds);
            testCaseFileMapper.deleteByExample(testCaseFileExample);
        }


        if (files != null) {
            files.forEach(file -> {
                final FileMetadata fileMetadata = fileService.saveFile(file, testCaseWithBLOBs.getProjectId());
                TestCaseFile testCaseFile = new TestCaseFile();
                testCaseFile.setFileId(fileMetadata.getId());
                testCaseFile.setCaseId(testCaseId);
                testCaseFileMapper.insert(testCaseFile);
            });
        }
        this.setNode(request);
        editTestCase(request);
        return request.getId();
    }

    public List<TestCaseDTO> listTestCaseIds(QueryTestCaseRequest request) {
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        // 对模板导入的测试用例排序
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        List<String> selectFields = new ArrayList<>();
        selectFields.add("id");
        selectFields.add("name");
        request.setSelectFields(selectFields);
        return extTestCaseMapper.list(request);
    }

    public void minderEdit(TestCaseMinderEditRequest request) {
        List<TestCaseWithBLOBs> data = request.getData();
        data.forEach(item -> {
            if (StringUtils.isBlank(item.getNodeId()) || item.getNodeId().equals("root")) {
                item.setNodeId("");
            }
            item.setProjectId(request.getProjectId());
            if (StringUtils.isBlank(item.getId()) || item.getId().length() < 20) {
                item.setId(UUID.randomUUID().toString());
                item.setMaintainer(SessionUtils.getUserId());
                addTestCase(item);
            } else {
                editTestCase(item);
            }
        });
        List<String> ids = request.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            TestCaseBatchRequest deleteRequest = new TestCaseBatchRequest();
            deleteRequest.setIds(ids);
            deleteTestCaseBath(deleteRequest);
        }
    }

    public List<TestCase> getTestCaseByProjectId(String projectId) {
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return testCaseMapper.selectByExample(example);
    }

    public List<TestCaseWithBLOBs> listTestCaseForMinder(QueryTestCaseRequest request) {
        return extTestCaseMapper.listForMinder(request);
    }

    public List<TestCaseDTO> getTestCaseByIds(List<String> testCaseIds) {
        if (CollectionUtils.isNotEmpty(testCaseIds)) {
            return extTestCaseMapper.getTestCaseByIds(testCaseIds);
        } else {
            return new ArrayList<>();
        }
    }

    public List<TestCaseDTO> getTestCaseIssueRelateList(QueryTestCaseRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return getTestCaseByNotInIssue(request);
    }

    /**
     * 更新项目下用例的CustomNum值
     * @param projectId 项目ID
     */
    public void updateTestCaseCustomNumByProjectId(String projectId) {
        extTestCaseMapper.updateTestCaseCustomNumByProjectId(projectId);
    }
}
