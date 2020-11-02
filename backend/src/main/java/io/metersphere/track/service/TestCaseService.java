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
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.domain.TestCaseExcelDataFactory;
import io.metersphere.excel.listener.EasyExcelListener;
import io.metersphere.excel.listener.TestCaseDataListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.service.FileService;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.EditTestCaseRequest;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.request.testcase.TestCaseBatchRequest;
import io.metersphere.xmind.XmindCaseParser;
import org.apache.commons.collections4.ListUtils;
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

    private TestCaseWithBLOBs addTestCase(TestCaseWithBLOBs testCase) {
        testCase.setName(testCase.getName());
        checkTestCaseExist(testCase);
        testCase.setId(UUID.randomUUID().toString());
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        testCase.setNum(getNextNum(testCase.getProjectId()));
        testCase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
        testCaseMapper.insert(testCase);
        return testCase;
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
        testCaseCommentService.deleteCaseComment(testCaseId);
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
        List<OrderRequest> orderList = ServiceUtils.getDefaultOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        return extTestCaseMapper.getTestCaseByNotInPlan(request);
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
        testCaseExample.createCriteria().andProjectIdIn(projectIds).andMaintainerEqualTo(request.getUserId());
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


    public ExcelResponse testCaseImport(MultipartFile multipartFile, String projectId, String userId) {

        ExcelResponse excelResponse = new ExcelResponse();
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
                if (xmindParser.getNodePaths().isEmpty() && xmindParser.getTestCase().isEmpty()) {
                    if (errList == null) {
                        errList = new ArrayList<>();
                    }
                    ExcelErrData excelErrData = new ExcelErrData(null, 1, Translator.get("upload_fail") + "：" + Translator.get("upload_content_is_null"));
                    errList.add(excelErrData);
                    excelResponse.setErrList(errList);
                }
                if (errList.isEmpty()) {
                    if (!xmindParser.getNodePaths().isEmpty()) {
                        testCaseNodeService.createNodes(xmindParser.getNodePaths(), projectId);
                    }
                    if (!xmindParser.getTestCase().isEmpty()) {
                        Collections.reverse(xmindParser.getTestCase());
                        this.saveImportData(xmindParser.getTestCase(), projectId);
                        xmindParser.clear();
                    }
                }
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
                Class clazz = new TestCaseExcelDataFactory().getExcelDataByLocal();
                EasyExcelListener easyExcelListener = new TestCaseDataListener(clazz, projectId, testCaseNames, userIds);
                EasyExcelFactory.read(multipartFile.getInputStream(), clazz, easyExcelListener).sheet().doRead();
                errList = easyExcelListener.getErrList();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(e.getMessage());
            }

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
                testcase.setId(UUID.randomUUID().toString());
                testcase.setCreateTime(System.currentTimeMillis());
                testcase.setUpdateTime(System.currentTimeMillis());
                testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                testcase.setSort(sort.getAndIncrement());
                testcase.setNum(num.decrementAndGet());
                testcase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
                mapper.insert(testcase);
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
                String setp = "";
                if (steps.contains("null")) {
                    setp = steps.replace("null", "\"\"");
                } else {
                    setp = steps;
                }
                JSONArray jsonArray = JSON.parseArray(setp);
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
                if (t.getTestId() != null && t.getTestId().equals("other")) {
                    data.setRemark(t.getOtherTestName());
                } else {
                    data.setRemark("[" + t.getApiName() + "]" + "\n" + t.getRemark());
                }

            } else if (t.getMethod().equals("auto") && t.getType().equals("performance")) {
                data.setStepDesc("");
                data.setStepResult("");
                if (t.getTestId() != null && t.getTestId().equals("other")) {
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

    public String save(EditTestCaseRequest request, List<MultipartFile> files) {
        if (files == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }

        final TestCaseWithBLOBs testCaseWithBLOBs = addTestCase(request);

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
            final FileMetadata fileMetadata = fileService.saveFile(file);
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
                final FileMetadata fileMetadata = fileService.saveFile(file);
                TestCaseFile testCaseFile = new TestCaseFile();
                testCaseFile.setFileId(fileMetadata.getId());
                testCaseFile.setCaseId(request.getId());
                testCaseFileMapper.insert(testCaseFile);
            });
        }

        editTestCase(request);
        return request.getId();
    }
}
