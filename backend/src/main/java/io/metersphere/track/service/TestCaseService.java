package io.metersphere.track.service;


import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.base.mapper.ext.ExtProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.constants.IssueRefType;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.constants.TestCaseReviewStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.controller.request.ProjectVersionRequest;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.dto.*;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.domain.TestCaseExcelDataFactory;
import io.metersphere.excel.handler.FunctionCaseTemplateWriteHandler;
import io.metersphere.excel.listener.TestCaseNoModelDataListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.excel.utils.FunctionCaseImportEnum;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestCaseReference;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.*;
import io.metersphere.track.dto.TestCaseCommentDTO;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.*;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.xmind.XmindCaseParser;
import io.metersphere.xmind.pojo.TestCaseXmindData;
import io.metersphere.xmind.utils.XmindExportUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    ExtIssuesMapper extIssuesMapper;

    @Resource
    UserService userService;

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
    ApiTestCaseMapper apiTestCaseMapper;

    @Resource
    TestCaseIssueService testCaseIssueService;
    @Resource
    TestCaseCommentService testCaseCommentService;
    @Resource
    FileService fileService;
    @Resource
    TestCaseFileMapper testCaseFileMapper;
    @Resource
    TestCaseTestMapper testCaseTestMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestCaseIssuesMapper testCaseIssuesMapper;
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private RelationshipEdgeService relationshipEdgeService;
    @Resource
    @Lazy
    private ApiTestCaseService apiTestCaseService;
    @Resource
    @Lazy
    private ApiAutomationService apiAutomationService;
    @Resource
    @Lazy
    private PerformanceTestService performanceTestService;
    @Resource
    private TestCaseFollowMapper testCaseFollowMapper;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private MinderExtraNodeService minderExtraNodeService;
    @Resource
    @Lazy
    private IssuesService issuesService;
    @Resource
    private RelationshipEdgeMapper relationshipEdgeMapper;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Lazy
    @Resource
    private ProjectApplicationService projectApplicationService;

    private ThreadLocal<Integer> importCreateNum = new ThreadLocal<>();
    private ThreadLocal<Integer> beforeImportCreateNum = new ThreadLocal<>();

    private void setNode(TestCaseWithBLOBs testCase) {
        if (StringUtils.isEmpty(testCase.getNodeId()) || "default-module".equals(testCase.getNodeId())) {
            TestCaseNodeExample example = new TestCaseNodeExample();
            example.createCriteria().andProjectIdEqualTo(testCase.getProjectId()).andNameEqualTo("未规划用例");
            List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(nodes)) {
                testCase.setNodeId(nodes.get(0).getId());
                testCase.setNodePath("/" + nodes.get(0).getName());
            }
        }
    }

    public TestCaseWithBLOBs addTestCase(EditTestCaseRequest request) {
        request.setName(request.getName());
        checkTestCaseExist(request);
        request.setId(request.getId());
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        checkTestCustomNum(request);
        request.setNum(getNextNum(request.getProjectId()));
        if (StringUtils.isBlank(request.getCustomNum())) {
            request.setCustomNum(request.getNum().toString());
        }
        request.setReviewStatus(TestCaseReviewStatus.Prepare.name());
        request.setStatus(TestCaseReviewStatus.Prepare.name());
        request.setDemandId(request.getDemandId());
        request.setDemandName(request.getDemandName());
        request.setCreateUser(SessionUtils.getUserId());
        this.setNode(request);
        request.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extTestCaseMapper::getLastOrder));
        //直接点保存 || 复制走的逻辑
        if (StringUtils.isAllBlank(request.getRefId(), request.getVersionId())) {
            //新创建测试用例，默认使用最新版本
            request.setRefId(request.getId());
            request.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        } else if (StringUtils.isBlank(request.getRefId()) && StringUtils.isNotBlank(request.getVersionId())) {
            //从版本选择直接创建
            request.setRefId(request.getId());
        }
        //完全新增一条记录直接就是最新
        request.setLatest(true);
        testCaseMapper.insert(request);
        saveFollows(request.getId(), request.getFollows());
        return request;
    }

    private void dealWithCopyOtherInfo(TestCaseWithBLOBs testCase, String oldTestCaseId) {
        EditTestCaseRequest request = new EditTestCaseRequest();
        BeanUtils.copyBean(request, testCase);
        EditTestCaseRequest.OtherInfoConfig otherInfoConfig = EditTestCaseRequest.OtherInfoConfig.createDefault();
        request.setOtherInfoConfig(otherInfoConfig);
        DealWithOtherInfo(request, oldTestCaseId);
    }

    public void saveFollows(String caseId, List<String> follows) {
        TestCaseFollowExample example = new TestCaseFollowExample();
        example.createCriteria().andCaseIdEqualTo(caseId);
        testCaseFollowMapper.deleteByExample(example);
        if (!CollectionUtils.isEmpty(follows)) {
            for (String follow : follows) {
                TestCaseFollow caseFollow = new TestCaseFollow();
                caseFollow.setCaseId(caseId);
                caseFollow.setFollowId(follow);
                testCaseFollowMapper.insert(caseFollow);
            }
        }
    }

    private void checkTestCustomNum(TestCaseWithBLOBs testCase) {
        if (StringUtils.isNotBlank(testCase.getCustomNum())) {
            String projectId = testCase.getProjectId();
            Project project = projectService.getProjectById(projectId);
            if (project != null) {
                ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
                boolean customNum = config.getCaseCustomNum();
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
        String id = testCase.getId();
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(id);
        TestCaseExample example = new TestCaseExample();
        TestCaseExample.Criteria criteria = example.createCriteria();
        criteria.andCustomNumEqualTo(testCase.getCustomNum())
                .andProjectIdEqualTo(testCase.getProjectId())
                .andIdNotEqualTo(testCase.getId());
        if (testCaseWithBLOBs != null && StringUtils.isNotBlank(testCaseWithBLOBs.getRefId())) {
            criteria.andRefIdNotEqualTo(testCaseWithBLOBs.getRefId());
        }
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

    public TestCaseWithBLOBs editTestCase(EditTestCaseRequest testCase) {
        checkTestCustomNum(testCase);
        testCase.setUpdateTime(System.currentTimeMillis());
        // 更新数据
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdEqualTo(testCase.getId());
        if (StringUtils.isNotBlank(testCase.getVersionId())) {
            example.getOredCriteria().get(0).andVersionIdEqualTo(testCase.getVersionId());
        }
        createNewVersionOrNot(testCase, example);

        if (StringUtils.isNotBlank(testCase.getCustomNum()) && StringUtils.isNotBlank(testCase.getId())) {
            TestCaseWithBLOBs caseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCase.getId());
            if (caseWithBLOBs != null) {
                example.clear();
                example.createCriteria().andRefIdEqualTo(caseWithBLOBs.getRefId());
                TestCaseWithBLOBs testCaseWithBLOBs = new TestCaseWithBLOBs();
                testCaseWithBLOBs.setCustomNum(testCase.getCustomNum());
                testCaseMapper.updateByExampleSelective(testCaseWithBLOBs, example);
            }
        }

        testCase.setLatest(null);
        testCaseMapper.updateByPrimaryKeySelective(testCase);
        return testCaseMapper.selectByPrimaryKey(testCase.getId());
    }

    /**
     * 根据前后端 verionId 判定是编辑旧数据还是创建新版本
     *
     * @param testCase
     * @param example
     */
    private void createNewVersionOrNot(EditTestCaseRequest testCase, TestCaseExample example) {
        if (testCaseMapper.updateByExampleSelective(testCase, example) == 0) {
            // 插入新版本的数据
            TestCaseWithBLOBs oldTestCase = testCaseMapper.selectByPrimaryKey(testCase.getId());
            testCase.setId(UUID.randomUUID().toString());
            testCase.setNum(oldTestCase.getNum());
            testCase.setCreateTime(System.currentTimeMillis());
            testCase.setUpdateTime(System.currentTimeMillis());
            testCase.setCreateUser(SessionUtils.getUserId());
            testCase.setOrder(oldTestCase.getOrder());
            testCase.setRefId(oldTestCase.getRefId());
            testCase.setLatest(false);
            DealWithOtherInfo(testCase, oldTestCase.getId());
            testCaseMapper.insertSelective(testCase);
            checkAndSetLatestVersion(testCase.getRefId(), testCase.getVersionId(), testCase.getProjectId(), "add");
        }
    }

    /**
     * 处理其他信息的复制问题
     *
     * @param testCase
     * @param oldTestCaseId
     */
    private void DealWithOtherInfo(EditTestCaseRequest testCase, String oldTestCaseId) {
        EditTestCaseRequest.OtherInfoConfig otherInfoConfig = testCase.getOtherInfoConfig();
        if (otherInfoConfig != null) {
            if (!otherInfoConfig.isRemark()) {
                testCase.setRemark(null);
            }
            if (!otherInfoConfig.isRelateDemand()) {
                testCase.setDemandId(null);
                testCase.setDemandName(null);
            }
            if (otherInfoConfig.isRelateIssue()) {
                List<IssuesDao> issuesDaos = issuesService.getIssues(oldTestCaseId, IssueRefType.FUNCTIONAL.name());
                if (CollectionUtils.isNotEmpty(issuesDaos)) {
                    issuesDaos.forEach(issue -> {
                        TestCaseIssues t = new TestCaseIssues();
                        t.setId(UUID.randomUUID().toString());
                        t.setResourceId(testCase.getId());
                        t.setIssuesId(issue.getId());
                        testCaseIssuesMapper.insertSelective(t);
                    });
                }
            }
            if (otherInfoConfig.isRelateTest()) {
                List<TestCaseTestDao> testCaseTestDaos = getRelateTest(oldTestCaseId);
                if (CollectionUtils.isNotEmpty(testCaseTestDaos)) {
                    testCaseTestDaos.forEach(test -> {
                        test.setTestCaseId(testCase.getId());
                        testCaseTestMapper.insertSelective(test);
                    });
                }
            }
            if (otherInfoConfig.isArchive()) {
                List<FileMetadata> files = fileService.getFileMetadataByCaseId(oldTestCaseId);
                if (CollectionUtils.isNotEmpty(files)) {
                    files.forEach(file -> {
                        TestCaseFile testCaseFile = new TestCaseFile();
                        testCaseFile.setCaseId(testCase.getId());
                        testCaseFile.setFileId(file.getId());
                        testCaseFileMapper.insertSelective(testCaseFile);
                    });
                }
            }
            if (otherInfoConfig.isDependency()) {
                List<RelationshipEdge> preRelations = relationshipEdgeService.getRelationshipEdgeByType(oldTestCaseId, "PRE");
                List<RelationshipEdge> postRelations = relationshipEdgeService.getRelationshipEdgeByType(oldTestCaseId, "POST");
                if (CollectionUtils.isNotEmpty(preRelations)) {
                    preRelations.forEach(relation -> {
                        relation.setSourceId(testCase.getId());
                        relation.setCreateTime(System.currentTimeMillis());
                        relationshipEdgeMapper.insertSelective(relation);
                    });
                }
                if (CollectionUtils.isNotEmpty(postRelations)) {
                    postRelations.forEach(relation -> {
                        relation.setTargetId(testCase.getId());
                        relation.setCreateTime(System.currentTimeMillis());
                        relationshipEdgeMapper.insertSelective(relation);
                    });
                }
            }

        }
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
                    .andTypeEqualTo(testCase.getType());
            if (StringUtils.isNotBlank(testCase.getPriority())) {
                criteria.andPriorityEqualTo(testCase.getPriority());
            }

            if (StringUtils.isNotBlank(testCase.getTestId())) {
                criteria.andTestIdEqualTo(testCase.getTestId());
            }

            if (StringUtils.isNotBlank(testCase.getId())) {
                criteria.andIdNotEqualTo(testCase.getId());
            }

            List<TestCaseWithBLOBs> caseList = testCaseMapper.selectByExampleWithBLOBs(example);

            // 如果上边字段全部相同，去检查 remark 和 steps
            if (!CollectionUtils.isEmpty(caseList)) {
                String caseRemark = testCase.getRemark() == null ? "" : testCase.getRemark();
                String caseSteps = testCase.getSteps() == null ? "" : testCase.getSteps();
                String casePrerequisite = testCase.getPrerequisite() == null ? "" : testCase.getPrerequisite();
                for (TestCaseWithBLOBs tc : caseList) {
                    String steps = tc.getSteps() == null ? "" : tc.getSteps();
                    String remark = tc.getRemark() == null ? "" : tc.getRemark();
                    String prerequisite = tc.getPrerequisite() == null ? "" : tc.getPrerequisite();
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
    public String checkIdExist(Integer id, String projectId) {
        TestCaseExample example = new TestCaseExample();
        TestCaseExample.Criteria criteria = example.createCriteria();
        if (null != id) {
            criteria.andNumEqualTo(id);
            criteria.andProjectIdEqualTo(projectId);
            List<TestCase> testCaseList = testCaseMapper.selectByExample(example);    //查询是否有包含此ID的数据
            if (testCaseList.isEmpty()) {  //如果ID不存在
                return null;
            } else { //有对应ID的数据
                return testCaseList.get(0).getId();
            }
        }
        return null;
    }

    public String checkCustomIdExist(String id, String projectId) {
        TestCaseExample example = new TestCaseExample();
        TestCaseExample.Criteria criteria = example.createCriteria();
        if (null != id) {
            criteria.andCustomNumEqualTo(id);
            criteria.andProjectIdEqualTo(projectId);
            List<TestCase> testCaseList = testCaseMapper.selectByExample(example);    //查询是否有包含此ID的数据
            if (testCaseList.isEmpty()) {  //如果ID不存在
                return null;
            } else { //有对应ID的数据
                return testCaseList.get(0).getId();
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
        relateDelete(testCaseId);
        relationshipEdgeService.delete(testCaseId); // 删除关系图
        deleteFollows(testCaseId);
        return testCaseMapper.deleteByPrimaryKey(testCaseId);
    }

    public int deleteTestCaseBySameVersion(String testCaseId) {
        TestCase testCase = testCaseMapper.selectByPrimaryKey(testCaseId);
        if (testCase == null) {
            return 0;
        }
        if (StringUtils.isNotBlank(testCase.getRefId())) {
            TestCaseExample testCaseExample = new TestCaseExample();
            testCaseExample.createCriteria().andRefIdEqualTo(testCase.getRefId());
            // 因为删除
            List<String> sameVersionIds = testCaseMapper.selectByExample(testCaseExample).stream().map(TestCase::getId).collect(Collectors.toList());
            AtomicInteger integer = new AtomicInteger(0);
            sameVersionIds.forEach(id -> integer.getAndAdd(deleteTestCase(id)));
            return integer.get();
        } else {
            return deleteTestCase(testCaseId);
        }
    }

    private void deleteFollows(String testCaseId) {
        TestCaseFollowExample example = new TestCaseFollowExample();
        example.createCriteria().andCaseIdEqualTo(testCaseId);
        testCaseFollowMapper.deleteByExample(example);
    }

    public int deleteTestCaseToGc(String testCaseId) {
        TestCase testCase = new TestCase();
        testCase.setId(testCaseId);
        testCase.setDeleteUserId(SessionUtils.getUserId());
        testCase.setDeleteTime(System.currentTimeMillis());
        return extTestCaseMapper.deleteToGc(testCase);
    }

    public List<TestCaseDTO> listTestCase(QueryTestCaseRequest request) {
        this.initRequest(request, true);
        setDefaultOrder(request);
        if (request.getFilters() != null && !request.getFilters().containsKey("status")) {
            request.getFilters().put("status", new ArrayList<>(0));
        }
        List<TestCaseDTO> list = extTestCaseMapper.list(request);
        buildUserInfo(list);
        if (StringUtils.isNotBlank(request.getProjectId())) {
            buildProjectInfo(request.getProjectId(), list);
        }else{
            buildProjectInfoWidthoutProject(list);
        }
        list = this.parseStatus(list);
        return list;
    }

    private void buildProjectInfoWidthoutProject(List<TestCaseDTO> resList) {
        resList.forEach(i -> {
            Project project = projectMapper.selectByPrimaryKey(i.getProjectId());
            i.setProjectName(project.getName());
        });
    }

    public List<TestCaseDTO> publicListTestCase(QueryTestCaseRequest request) {
        this.initRequest(request, true);
        setDefaultOrder(request);
        if (request.getFilters() != null && !request.getFilters().containsKey("status")) {
            request.getFilters().put("status", new ArrayList<>(0));
        }
        List<TestCaseDTO> returnList = extTestCaseMapper.publicList(request);
        returnList = this.parseStatus(returnList);
        return returnList;
    }


    public void setDefaultOrder(QueryTestCaseRequest request) {
        List<OrderRequest> orders = ServiceUtils.getDefaultSortOrder(request.getOrders());
        OrderRequest order = new OrderRequest();
        // 对模板导入的测试用例排序
        order.setName("sort");
        order.setType("desc");
        orders.add(order);
        orders.forEach(i -> i.setPrefix("test_case"));
        request.setOrders(orders);
    }

    private List<TestCaseDTO> parseStatus(List<TestCaseDTO> returnList) {
        TestCaseExcelData excelData = new TestCaseExcelDataFactory().getTestCaseExcelDataLocal();
        for (TestCaseDTO data : returnList) {
            String lastStatus = extTestCaseMapper.getLastExecStatusById(data.getId());
            if (StringUtils.isNotEmpty(lastStatus)) {
                data.setLastExecuteResult(lastStatus);
            } else {
                data.setLastExecuteResult(null);
            }
            String dataStatus = excelData.parseStatus(data.getStatus());

            if (StringUtils.equalsAnyIgnoreCase(data.getStatus(), "Trash")) {
                try {
                    JSONArray arr = JSONArray.parseArray(data.getCustomFields());
                    JSONArray newArr = new JSONArray();
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        if (obj.containsKey("name") && obj.containsKey("value")) {
                            String name = obj.getString("name");
                            if (StringUtils.equalsAny(name, "用例状态", "用例狀態", "Case status")) {
                                obj.put("value", dataStatus);
                            }
                        }
                        newArr.add(obj);
                    }
                    data.setCustomFields(newArr.toJSONString());
                } catch (Exception e) {

                }
            }

            data.setStatus(dataStatus);
        }
        return returnList;
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
    public Pager<List<TestCaseDTO>> getTestCaseRelateList(QueryTestCaseRequest request, int goPage, int pageSize) {
        setDefaultOrder(request);
        request.getOrders().forEach(order -> {
            order.setPrefix("test_case");
        });
        if (testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setRepeatCase(true);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, getTestCaseByNotInPlan(request));
    }

    public List<TestCaseDTO> getTestCaseByNotInPlan(QueryTestCaseRequest request) {
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

    public List<TestCaseDTO> getReviewCase(QueryTestCaseRequest request) {
        setDefaultOrder(request);
        request.getOrders().forEach(order -> {
            order.setPrefix("test_case");
        });
        return extTestCaseMapper.getTestCaseByNotInReview(request);
    }


    public List<TestCase> recentTestPlans(QueryTestCaseRequest request, int count) {
        PageHelper.startPage(1, count, true);
        TestCaseExample testCaseExample = new TestCaseExample();
        TestCaseExample.Criteria criteria = testCaseExample.createCriteria();
        criteria.andMaintainerEqualTo(request.getUserId());
        if (StringUtils.isNotBlank(request.getProjectId())) {
            criteria.andProjectIdEqualTo(request.getProjectId());
            testCaseExample.setOrderByClause("order desc, sort desc");
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


    public ExcelResponse testCaseImport(MultipartFile multipartFile, TestCaseImportRequest request, HttpServletRequest httpRequest) {
        if (multipartFile == null) {
            MSException.throwException(Translator.get("upload_fail"));
        }
        if (StringUtils.equals(request.getImportType(), FunctionCaseImportEnum.Create.name())) {
            // 创建如果没选版本就创建最新版本，更新时没选就更新最近版本的用例
            if (StringUtils.isBlank(request.getVersionId())) {
                request.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
            }
            int nextNum = getNextNum(request.getProjectId());
            importCreateNum.set(nextNum);
            beforeImportCreateNum.set(nextNum);
        }
        if (multipartFile.getOriginalFilename().endsWith(".xmind")) {
            return testCaseXmindImport(multipartFile, request, httpRequest);
        } else {
            return testCaseExcelImport(multipartFile, request, httpRequest);
        }
    }

    private List<TestCase> getTestCaseForImport(String projectId) {
        QueryTestCaseRequest queryTestCaseRequest = new QueryTestCaseRequest();
        queryTestCaseRequest.setProjectId(projectId);
        return extTestCaseMapper.getTestCaseNames(queryTestCaseRequest);
    }

    private ExcelResponse getImportResponse(List<ExcelErrData<TestCaseExcelData>> errList, boolean isUpdated) {
        ExcelResponse excelResponse = new ExcelResponse();
        excelResponse.setIsUpdated(isUpdated);
        //如果包含错误信息就导出错误信息
        if (!errList.isEmpty()) {
            excelResponse.setSuccess(false);
            excelResponse.setErrList(errList);
        } else {
            excelResponse.setSuccess(true);
        }
        return excelResponse;
    }

    private ExcelResponse testCaseXmindImport(MultipartFile multipartFile, TestCaseImportRequest request,
                                              HttpServletRequest httpRequest) {
        String projectId = request.getProjectId();
        List<ExcelErrData<TestCaseExcelData>> errList = new ArrayList<>();
        Project project = projectService.getProjectById(projectId);
        boolean useCunstomId = projectService.useCustomNum(project);

        Set<String> testCaseNames = getTestCaseForImport(projectId).stream()
                .map(TestCase::getName)
                .collect(Collectors.toSet());

        try {
            request.setTestCaseNames(testCaseNames);
            request.setUseCustomId(useCunstomId);
            XmindCaseParser xmindParser = new XmindCaseParser(request);
            errList = xmindParser.parse(multipartFile);
            if (CollectionUtils.isEmpty(xmindParser.getNodePaths())
                    && CollectionUtils.isEmpty(xmindParser.getTestCase())
                    && CollectionUtils.isEmpty(xmindParser.getUpdateTestCase())) {
                if (errList == null) {
                    errList = new ArrayList<>();
                }
                ExcelErrData excelErrData = new ExcelErrData(null, 1, Translator.get("upload_fail") + "：" + Translator.get("upload_content_is_null"));
                errList.add(excelErrData);
            }

            List<String> names = new LinkedList<>();
            List<String> ids = new LinkedList<>();
            if (!request.isIgnore()) {
                if (errList.isEmpty()) {
                    if (CollectionUtils.isNotEmpty(xmindParser.getNodePaths())) {
                        testCaseNodeService.createNodes(xmindParser.getNodePaths(), projectId);
                    }
                    if (CollectionUtils.isNotEmpty(xmindParser.getTestCase())) {
//                        Collections.reverse(xmindParser.getTestCase());
                        this.saveImportData(xmindParser.getTestCase(), request);
                        names = xmindParser.getTestCase().stream().map(TestCase::getName).collect(Collectors.toList());
                        ids = xmindParser.getTestCase().stream().map(TestCase::getId).collect(Collectors.toList());
                    }
                    if (CollectionUtils.isNotEmpty(xmindParser.getUpdateTestCase())) {
                        this.updateImportData(xmindParser.getUpdateTestCase(), request);
                        names.addAll(xmindParser.getUpdateTestCase().stream().map(TestCase::getName).collect(Collectors.toList()));
                        ids.addAll(xmindParser.getUpdateTestCase().stream().map(TestCase::getId).collect(Collectors.toList()));
                    }
                    httpRequest.setAttribute("ms-req-title", String.join(",", names));
                    httpRequest.setAttribute("ms-req-source-id", JSON.toJSONString(ids));
                }
            } else {
                List<TestCaseWithBLOBs> continueCaseList = xmindParser.getContinueValidatedCase();
                if (CollectionUtils.isNotEmpty(continueCaseList) || CollectionUtils.isNotEmpty(xmindParser.getUpdateTestCase())) {
                    if (CollectionUtils.isNotEmpty(xmindParser.getUpdateTestCase())) {
                        continueCaseList.removeAll(xmindParser.getUpdateTestCase());
                        this.updateImportData(xmindParser.getUpdateTestCase(), request);
                        names = xmindParser.getTestCase().stream().map(TestCase::getName).collect(Collectors.toList());
                        ids = xmindParser.getTestCase().stream().map(TestCase::getId).collect(Collectors.toList());
                    }
                    List<String> nodePathList = xmindParser.getValidatedNodePath();
                    if (CollectionUtils.isNotEmpty(nodePathList)) {
                        testCaseNodeService.createNodes(nodePathList, projectId);
                    }
                    if (CollectionUtils.isNotEmpty(continueCaseList)) {
//                        Collections.reverse(continueCaseList);
                        this.saveImportData(continueCaseList, request);
                        names.addAll(continueCaseList.stream().map(TestCase::getName).collect(Collectors.toList()));
                        ids.addAll(continueCaseList.stream().map(TestCase::getId).collect(Collectors.toList()));

                    }
                    httpRequest.setAttribute("ms-req-title", String.join(",", names));
                    httpRequest.setAttribute("ms-req-source-id", JSON.toJSONString(ids));
                }

            }
            xmindParser.clear();
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e.getMessage());
        }
        return getImportResponse(errList, true);
    }

    private ExcelResponse testCaseExcelImport(MultipartFile multipartFile, TestCaseImportRequest request,
                                              HttpServletRequest httpRequest) {
        String projectId = request.getProjectId();
        Set<String> userIds;
        Project project = projectService.getProjectById(projectId);
        boolean useCunstomId = projectService.useCustomNum(project);

        Set<String> savedIds = new HashSet<>();
        Set<String> testCaseNames = new HashSet<>();
        List<ExcelErrData<TestCaseExcelData>> errList = new ArrayList<>();
        boolean isUpdated = false;

        List<TestCase> testCases = getTestCaseForImport(projectId);
        for (TestCase testCase : testCases) {
            if (useCunstomId) {
                savedIds.add(testCase.getCustomNum());
            } else {
                savedIds.add(String.valueOf(testCase.getNum()));
            }

            testCaseNames.add(testCase.getName());
        }

        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setProjectId(projectId);
        userIds = userService.getProjectMemberList(queryMemberRequest)
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        try {
            //根据本地语言环境选择用哪种数据对象进行存放读取的数据
            Class clazz = new TestCaseExcelDataFactory().getExcelDataByLocal();
            TestCaseTemplateService testCaseTemplateService = CommonBeanFactory.getBean(TestCaseTemplateService.class);
            TestCaseTemplateDao testCaseTemplate = testCaseTemplateService.getTemplate(projectId);
            List<CustomFieldDao> customFields = null;
            if (testCaseTemplate == null) {
                customFields = new ArrayList<>();
            } else {
                customFields = testCaseTemplate.getCustomFields();
            }
            request.setUserIds(userIds);
            request.setTestCaseNames(testCaseNames);
            request.setCustomFields(customFields);
            request.setSavedCustomIds(savedIds);
            request.setUseCustomId(useCunstomId);
            TestCaseNoModelDataListener easyExcelListener = new TestCaseNoModelDataListener(request, clazz);

            //读取excel数据
            EasyExcelFactory.read(multipartFile.getInputStream(), easyExcelListener).sheet().doRead();
            httpRequest.setAttribute("ms-req-title", String.join(",", easyExcelListener.getNames()));
            httpRequest.setAttribute("ms-req-source-id", JSON.toJSONString(easyExcelListener.getIds()));

            errList = easyExcelListener.getErrList();
            isUpdated = easyExcelListener.isUpdated();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        return getImportResponse(errList, isUpdated);
    }

    public void saveImportData(List<TestCaseWithBLOBs> testCases, TestCaseImportRequest request) {
        String projectId = request.getProjectId();
        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        Project project = projectService.getProjectById(projectId);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
        boolean customNum = config.getCaseCustomNum();
        try {
            if (!testCases.isEmpty()) {
                Integer num = importCreateNum.get();
                Integer beforeInsertId = beforeImportCreateNum.get();

                for (int i = testCases.size() - 1; i > -1; i--) { // 反向遍历，保持和文件顺序一致
                    TestCaseWithBLOBs testCase = testCases.get(i);
                    testCase.setId(UUID.randomUUID().toString());
                    testCase.setCreateUser(SessionUtils.getUserId());
                    testCase.setCreateTime(System.currentTimeMillis());
                    testCase.setUpdateTime(System.currentTimeMillis());
                    if (StringUtils.isNotBlank(testCase.getNodePath())) {
                        String[] modules = testCase.getNodePath().split("/");
                        StringBuilder path = new StringBuilder();
                        for (String module : modules) {
                            if (StringUtils.isNotBlank(module)) {
                                path.append("/");
                                path.append(module.trim());
                            }
                        }
                        testCase.setNodePath(path.toString());
                    }
                    testCase.setNodeId(nodePathMap.get(testCase.getNodePath()));
                    testCase.setNum(num);
                    if (customNum && StringUtils.isBlank(testCase.getCustomNum())) {
                        testCase.setCustomNum(String.valueOf(num));
                    }
                    num++;
                    testCase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
                    testCase.setStatus(TestCaseReviewStatus.Prepare.name());
                    testCase.setOrder(new Long(testCases.size() - (num - beforeInsertId)) * ServiceUtils.ORDER_STEP);
                    testCase.setRefId(testCase.getId());
                    testCase.setVersionId(request.getVersionId());
                    testCase.setLatest(true);
                    mapper.insert(testCase);
                }

                importCreateNum.set(num);
            }
        } finally {
            sqlSession.commit();
            sqlSession.clearCache();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }


    /**
     * 把Excel中带ID的数据更新到数据库
     * feat(测试跟踪):通过Excel导入导出时有ID字段，可通过Excel导入来更新用例。 (#1727)
     *
     * @param testCases
     * @param request
     */
    public void updateImportData(List<TestCaseWithBLOBs> testCases, TestCaseImportRequest request) {

        String projectId = request.getProjectId();
        List<TestCase> insertCases = new ArrayList<>();
        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        TestCaseExample example = new TestCaseExample();
        TestCaseExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);

        if (request.isUseCustomId()) {
            List<String> nums = testCases.stream()
                    .map(TestCase::getCustomNum)
                    .collect(Collectors.toList());
            criteria.andCustomNumIn(nums);
        } else {
            List<Integer> nums = testCases.stream()
                    .map(TestCase::getNum)
                    .collect(Collectors.toList());
            criteria.andNumIn(nums);
        }

        // 获取用例的“网页上所显示id”与“数据库ID”映射。
        Map<Object, TestCase> customIdMap;

        if (StringUtils.isBlank(request.getVersionId())) {
            // 没选版本更新最新版本
            criteria.andLatestEqualTo(true);
            List<TestCase> testCasesList = testCaseMapper.selectByExample(example);
            customIdMap = testCasesList.stream()
                    .collect(Collectors.toMap(i -> request.isUseCustomId() ? i.getCustomNum() : i.getNum(), i -> i, (k1, k2) -> k1));
        } else {
            List<TestCase> testCasesList = testCaseMapper.selectByExample(example);
            customIdMap = testCasesList.stream()
                    .collect(Collectors.toMap(i -> request.isUseCustomId() ? i.getCustomNum() : i.getNum(),
                            i -> i, (k1, k2) -> {
                                // 查找出来多条，选取当前版本的用例，没有的话随便保存一条，以便新建的时候设置对应的ref_id
                                if (k1.getVersionId().equals(request.getVersionId())) {
                                    return k1;
                                } else if (k2.getVersionId().equals(request.getVersionId())) {
                                    return k2;
                                }
                                return k1;
                            }));
        }

        try {
            if (!testCases.isEmpty()) {
                AtomicInteger sort = new AtomicInteger();
                testCases.forEach(testcase -> {
                    testcase.setUpdateTime(System.currentTimeMillis());
                    testcase.setNodeId(nodePathMap.get(testcase.getNodePath()));
                    testcase.setSort(sort.getAndIncrement());
                    TestCase dbCase = request.isUseCustomId() ? customIdMap.get(testcase.getCustomNum()) : customIdMap.get(testcase.getNum());
                    testcase.setId(dbCase.getId());
                    testcase.setRefId(dbCase.getRefId());
                    if (StringUtils.isBlank(request.getVersionId())) {
                        request.setVersionId(extProjectVersionMapper.getDefaultVersion(projectId));
                    }
                    // 选了版本就更新到对应的版本
                    if (dbCase.getVersionId().equals(request.getVersionId())) {
                        mapper.updateByPrimaryKeySelective(testcase);
                    } else { // 没有对应的版本就新建对应版本用例
                        testcase.setCreateTime(System.currentTimeMillis());
                        testcase.setVersionId(request.getVersionId());
                        testcase.setId(UUID.randomUUID().toString());
                        testcase.setOrder(dbCase.getOrder());
                        testcase.setCreateUser(SessionUtils.getUserId());
                        testcase.setCreateUser(SessionUtils.getUserId());
                        testcase.setCreateUser(SessionUtils.getUserId());
                        testcase.setCustomNum(dbCase.getCustomNum());
                        testcase.setNum(dbCase.getNum());
                        testcase.setLatest(false);
                        testcase.setType(dbCase.getType());
                        if (StringUtils.isBlank(testcase.getStatus())) {
                            testcase.setStatus(TestCaseReviewStatus.Prepare.name());
                        }
                        testcase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
                        insertCases.add(testcase); // 由于是批处理，这里先保存，最后再执行
                        mapper.insert(testcase);
                    }
                });
            }
            sqlSession.flushStatements();

            insertCases.forEach(item -> {
                checkAndSetLatestVersion(item.getRefId(), request.getVersionId(), request.getProjectId(), "add");
            });
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void testCaseTemplateExport(String projectId, String importType, HttpServletResponse response) {
        try {
            TestCaseExcelData testCaseExcelData = new TestCaseExcelDataFactory().getTestCaseExcelDataLocal();


            boolean useCustomNum = projectService.useCustomNum(projectId);
            boolean importFileNeedNum = false;
            if (useCustomNum || StringUtils.equals(importType, FunctionCaseImportEnum.Update.name())) {
                //导入更新 or 开启使用自定义ID时，导出ID列
                importFileNeedNum = true;
            }

            TestCaseTemplateService testCaseTemplateService = CommonBeanFactory.getBean(TestCaseTemplateService.class);
            TestCaseTemplateDao testCaseTemplate = testCaseTemplateService.getTemplate(projectId);
            List<CustomFieldDao> customFields = null;
            if (testCaseTemplate == null) {
                customFields = new ArrayList<>();
            } else {
                customFields = testCaseTemplate.getCustomFields();
            }

            List<List<String>> headList = testCaseExcelData.getHead(importFileNeedNum, customFields);
            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(testCaseExcelData.getClass());
            Map<String, List<String>> caseLevelAndStatusValueMap = testCaseTemplateService.getCaseLevelAndStatusMapByProjectId(projectId);
            FunctionCaseTemplateWriteHandler handler = new FunctionCaseTemplateWriteHandler(importFileNeedNum, headList, caseLevelAndStatusValueMap);
            easyExcelExporter.exportByCustomWriteHandler(response, headList, generateExportDatas(importFileNeedNum),
                    Translator.get("test_case_import_template_name"), Translator.get("test_case_import_template_sheet"), handler);

        } catch (Exception e) {
            MSException.throwException(e);
        }
    }

    public void download(String fileName, HttpServletResponse res) throws IOException {
        if (StringUtils.isEmpty(fileName)) {
            fileName = "xmind.xml";
        }
        // 发送给客户端的数据
        byte[] buff = new byte[1024];
        try (OutputStream outputStream = res.getOutputStream();
             BufferedInputStream bis = new BufferedInputStream(TestCaseService.class.getResourceAsStream("/io/metersphere/xmind/template/" + fileName));) {
            int i = bis.read(buff);
            while (i != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                i = bis.read(buff);
            }
        } catch (Exception ex) {
            LogUtil.error(ex);
            MSException.throwException("下载思维导图模版失败");
        }
    }

    public void testCaseXmindTemplateExport(String projectId, String importType, HttpServletResponse response) {
        try {
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            boolean isUseCustomId = projectService.useCustomNum(projectId);
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("思维导图用例模版", "UTF-8") + ".xmind");
            String fileName = null;
            if (StringUtils.equals(importType, FunctionCaseImportEnum.Update.name())) {
                fileName = "xmind_update.xml";
            } else {
                if (isUseCustomId) {
                    fileName = "xmind_custom_id.xml";
                } else {
                    fileName = "xmind_system_id.xml";
                }
            }
            download(fileName, response);
        } catch (Exception ex) {

        }
    }

    private List<List<Object>> generateExportDatas(boolean needCustomId) {
        List<List<Object>> list = new ArrayList<>();
        StringBuilder path = new StringBuilder("");
        List<String> types = TestCaseConstants.Type.getValues();
        SessionUser user = SessionUtils.getUser();
        for (int i = 1; i <= 5; i++) {
            List<Object> rowData = new ArrayList<>();
            if (needCustomId) {
                rowData.add("");
            }
            rowData.add(Translator.get("test_case") + i);
            path.append("/" + Translator.get("module") + i);
            rowData.add(path.toString());
            rowData.add("");
            rowData.add(Translator.get("preconditions_optional"));
            rowData.add(Translator.get("remark_optional"));
            rowData.add("1. " + Translator.get("step_tip_separate") + "\n2. " + Translator.get("step_tip_order") + "\n3. " + Translator.get("step_tip_optional"));
            rowData.add("1. " + Translator.get("result_tip_separate") + "\n2. " + Translator.get("result_tip_order") + "\n3. " + Translator.get("result_tip_optional"));
            rowData.add("");
            rowData.add("P" + i % 4);
            list.add(rowData);
        }
        return list;
    }

    private List<TestCaseExcelData> generateExportTemplate() {
        List<TestCaseExcelData> list = new ArrayList<>();
        StringBuilder path = new StringBuilder("");
        List<String> types = TestCaseConstants.Type.getValues();
        SessionUser user = SessionUtils.getUser();
        TestCaseExcelDataFactory factory = new TestCaseExcelDataFactory();
        for (int i = 1; i <= 5; i++) {
            TestCaseExcelData data = factory.getTestCaseExcelDataLocal();
            data.setName(Translator.get("test_case") + i);
            path.append("/" + Translator.get("module") + i);
            data.setNodePath(path.toString());
            data.setPriority("P" + i % 4);
            String type = types.get(i % 3);
            data.setPrerequisite(Translator.get("preconditions_optional"));
            data.setStepDesc("1. " + Translator.get("step_tip_separate") +
                    "\n2. " + Translator.get("step_tip_order") + "\n3. " + Translator.get("step_tip_optional"));
            data.setStepResult("1. " + Translator.get("result_tip_separate") + "\n2. " + Translator.get("result_tip_order") + "\n3. " + Translator.get("result_tip_optional"));
            data.setMaintainer(user.getId());
            data.setRemark(Translator.get("remark_optional"));
            list.add(data);
        }

        list.add(new TestCaseExcelData());
        return list;
    }

    public void testCaseExport(HttpServletResponse response, TestCaseBatchRequest request) {
        try {
//            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(new TestCaseExcelDataFactory().getExcelDataByLocal());
//            List<TestCaseExcelData> datas = generateTestCaseExcel(request);
//            easyExcelExporter.export(response,datas,Translator.get("test_case_import_template_name"), Translator.get("test_case_import_template_sheet"));

            TestCaseExcelData testCaseExcelData = new TestCaseExcelDataFactory().getTestCaseExcelDataLocal();
            List<TestCaseExcelData> datas = generateTestCaseExcel(request);
            boolean importFileNeedNum = true;
            TestCaseTemplateService testCaseTemplateService = CommonBeanFactory.getBean(TestCaseTemplateService.class);
            TestCaseTemplateDao testCaseTemplate = testCaseTemplateService.getTemplate(request.getProjectId());
            List<CustomFieldDao> customFields = Optional.ofNullable(testCaseTemplate.getCustomFields()).orElse(new ArrayList<>());

            List<List<String>> headList = testCaseExcelData.getHead(importFileNeedNum, customFields);
            List<List<Object>> testCaseDataByExcelList = this.generateTestCaseExcel(headList, datas);
            EasyExcelExporter easyExcelExporter = new EasyExcelExporter(testCaseExcelData.getClass());
            easyExcelExporter.exportByCustomWriteHandler(response, headList, testCaseDataByExcelList,
                    Translator.get("test_case_import_template_name"), Translator.get("test_case_import_template_sheet"));


        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }
    }


    public void testCaseXmindExport(HttpServletResponse response, TestCaseBatchRequest request) {
        try {
            request.getCondition().setStatusIsNot("Trash");
            List<TestCaseDTO> testCaseDTOList = this.findByBatchRequest(request);

            TestCaseXmindData rootXmindData = this.generateTestCaseXmind(testCaseDTOList);
            boolean isUseCustomId = projectService.useCustomNum(request.getProjectId());
            XmindExportUtil xmindExportUtil = new XmindExportUtil(isUseCustomId);
            xmindExportUtil.exportXmind(response, rootXmindData);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }
    }

    private TestCaseXmindData generateTestCaseXmind(List<TestCaseDTO> testCaseDTOList) {
        Map<String, List<TestCaseDTO>> moduleTestCaseMap = new HashMap<>();
        for (TestCaseDTO dto : testCaseDTOList) {
            String moduleId = dto.getNodeId();
            if (StringUtils.isEmpty(moduleId)) {
                moduleId = "default";
            }
            if (moduleTestCaseMap.containsKey(moduleId)) {
                moduleTestCaseMap.get(moduleId).add(dto);
            } else {
                List<TestCaseDTO> list = new ArrayList<>();
                list.add(dto);
                moduleTestCaseMap.put(moduleId, list);
            }
        }

        TestCaseXmindData rootMind = new TestCaseXmindData("ROOT", "ROOT");

        for (Map.Entry<String, List<TestCaseDTO>> entry : moduleTestCaseMap.entrySet()) {
            String moduleId = entry.getKey();
            List<TestCaseDTO> dataList = entry.getValue();

            if (StringUtils.equals(moduleId, "ROOT")) {
                rootMind.setTestCaseList(dataList);
            } else {
                LinkedList<TestCaseNode> modulePathDataList = testCaseNodeService.getPathNodeById(moduleId);
                rootMind.setItem(modulePathDataList, dataList);
            }
        }

        return rootMind;
    }

    private List<List<Object>> generateTestCaseExcel(List<List<String>> headListParams, List<TestCaseExcelData> datas) {
        List<List<Object>> returnDatas = new ArrayList<>();
        //转化excel头
        List<String> headList = new ArrayList<>();
        for (List<String> list : headListParams) {
            for (String head : list) {
                headList.add(head);
            }
        }

        for (TestCaseExcelData model : datas) {
            List<Object> list = new ArrayList<>();
            Map<String, String> customDataMaps = Optional.ofNullable(model.getCustomDatas()).orElse(new HashMap<>());

            for (String head : headList) {
                if (StringUtils.equalsAnyIgnoreCase(head, "ID")) {
                    list.add(model.getCustomNum());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Name", "用例名稱", "用例名称")) {
                    list.add(model.getName());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Module", "所屬模塊", "所属模块")) {
                    list.add(model.getNodePath());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Tag", "標簽", "标签")) {
                    String tags = "";
                    try {
                        if (model.getTags() != null) {
                            JSONArray arr = JSONArray.parseArray(model.getTags());
                            for (int i = 0; i < arr.size(); i++) {
                                tags += arr.getString(i) + ",";
                            }
                        }
                    } catch (Exception e) {
                    }
                    list.add(tags);
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Prerequisite", "前置條件", "前置条件")) {
                    list.add(model.getPrerequisite());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Remark", "備註", "备注")) {
                    list.add(model.getRemark());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Step description", "步驟描述", "步骤描述")) {
                    list.add(model.getStepDesc());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Step result", "預期結果", "预期结果")) {
                    list.add(model.getStepResult());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Edit Model", "編輯模式", "编辑模式")) {
                    list.add(model.getStepModel());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Priority", "用例等級", "用例等级")) {
                    list.add(model.getPriority());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Case status", "用例状态", "用例狀態")) {
                    list.add(model.getStatus());
                } else if (StringUtils.equalsAnyIgnoreCase(head, "Maintainer(ID)", "责任人(ID)", "維護人(ID)")) {
                    list.add(model.getMaintainer());
                } else {
                    String value = Optional.ofNullable(customDataMaps.get(head)).orElse("");
                    list.add(value);
                }
            }
            returnDatas.add(list);
        }

        return returnDatas;
    }

    public List<TestCaseDTO> findByBatchRequest(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        QueryTestCaseRequest condition = request.getCondition();
        List<OrderRequest> orderList = new ArrayList<>();
        if (condition != null) {
            orderList = ServiceUtils.getDefaultSortOrder(request.getOrders());
        }
        OrderRequest order = new OrderRequest();
        order.setName("sort");
        order.setType("desc");
        orderList.add(order);
        request.setOrders(orderList);
        List<TestCaseDTO> testCaseList = extTestCaseMapper.listByTestCaseIds(request);
        return testCaseList;
    }

    private List<TestCaseExcelData> generateTestCaseExcel(TestCaseBatchRequest request) {
        request.getCondition().setStatusIsNot("Trash");
        List<TestCaseDTO> testCaseList = this.findByBatchRequest(request);
        boolean isUseCustomId = projectService.useCustomNum(request.getProjectId());
        List<TestCaseExcelData> list = new ArrayList<>();
        StringBuilder step = new StringBuilder("");
        StringBuilder result = new StringBuilder("");

        Map<String, Map<String, String>> customSelectValueMap = new HashMap<>();
        TestCaseTemplateService testCaseTemplateService = CommonBeanFactory.getBean(TestCaseTemplateService.class);
        TestCaseTemplateDao testCaseTemplate = testCaseTemplateService.getTemplate(request.getProjectId());

        List<CustomFieldDao> customFieldList = null;
        if (testCaseTemplate == null) {
            customFieldList = new ArrayList<>();
        } else {
            customFieldList = testCaseTemplate.getCustomFields();
        }
        for (CustomFieldDao dto : customFieldList) {
            Map<String, String> map = new HashMap<>();
            if (StringUtils.equals("select", dto.getType())) {
                try {
                    JSONArray optionsArr = JSONArray.parseArray(dto.getOptions());
                    for (int i = 0; i < optionsArr.size(); i++) {
                        JSONObject obj = optionsArr.getJSONObject(i);
                        if (obj.containsKey("text") && obj.containsKey("value")) {
                            String value = obj.getString("value");
                            String text = obj.getString("text");
                            if (StringUtils.equals(text, "test_track.case.status_finished")) {
                                text = Translator.get("test_case_status_finished");
                            } else if (StringUtils.equals(text, "test_track.case.status_prepare")) {
                                text = Translator.get("test_case_status_prepare");
                            } else if (StringUtils.equals(text, "test_track.case.status_running")) {
                                text = Translator.get("test_case_status_running");
                            }
                            if (StringUtils.isNotEmpty(value)) {
                                map.put(value, text);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
            customSelectValueMap.put(dto.getName(), map);
        }


        testCaseList.forEach(t -> {
            TestCaseExcelData data = new TestCaseExcelData();
            data.setNum(t.getNum());
            data.setName(t.getName());
            data.setNodePath(t.getNodePath());
            data.setPriority(t.getPriority());
            if (isUseCustomId) {
                data.setCustomNum(t.getCustomNum());
            } else {
                data.setCustomNum(String.valueOf(t.getNum()));
            }
            if (StringUtils.isBlank(t.getStepModel())) {
                data.setStepModel(TestCaseConstants.StepModel.STEP.name());
            } else {
                data.setStepModel(t.getStepModel());
            }
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
                            step.append(num + "." + jsonArray.getJSONObject(j).getString("desc") + "\n");
                            result.append(num + "." + jsonArray.getJSONObject(j).getString("result") + "\n");

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
            data.setStatus(t.getStatus());
            String customFields = t.getCustomFields();
            try {
                JSONArray customFieldsArr = JSONArray.parseArray(customFields);
                Map<String, String> map = new HashMap<>();
                for (int index = 0; index < customFieldsArr.size(); index++) {
                    JSONObject obj = customFieldsArr.getJSONObject(index);
                    if (obj.containsKey("name") && obj.containsKey("value")) {
                        //进行key value对换
                        String name = obj.getString("name");
                        String value = obj.getString("value");
                        if (customSelectValueMap.containsKey(name)) {
                            if (customSelectValueMap.get(name).containsKey(value)) {
                                value = customSelectValueMap.get(name).get(value);
                            }
                        }
                        map.put(name, value);
                    }
                }
                data.setCustomDatas(map);
            } catch (Exception e) {
            }
            list.add(data);
        });
        return list;
    }

    /**
     * 更新自定义字段
     *
     * @param request
     */
    public void editTestCaseBath(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (request.getCustomField() != null) {
            List<TestCaseWithBLOBs> testCases = extTestCaseMapper.getCustomFieldsByIds(ids);
            testCases.forEach((testCase) -> {
                String customFields = testCase.getCustomFields();
                List<TestCaseBatchRequest.CustomFiledRequest> fields;
                if (StringUtils.isBlank(customFields)) {
                    fields = new ArrayList<>();
                } else {
                    fields = JSONObject.parseArray(customFields, TestCaseBatchRequest.CustomFiledRequest.class);
                }

                boolean hasField = false;
                for (TestCaseBatchRequest.CustomFiledRequest field : fields) {
                    if (StringUtils.equals(request.getCustomField().getName(), field.getName())) {
                        field.setValue(request.getCustomField().getValue());
                        hasField = true;
                        break;
                    }
                }
                if (!hasField) {
                    TestCaseBatchRequest.CustomFiledRequest customField = request.getCustomField();
                    customField.setId(request.getCustomTemplateFieldId());
                    customField.setName(request.getCustomField().getName());
                    customField.setValue(request.getCustomField().getValue());
                    fields.add(request.getCustomField());
                }
                if (StringUtils.equals(request.getCustomField().getName(), "用例等级")) {
                    testCase.setPriority((String) request.getCustomField().getValue());
                }
                testCase.setCustomFields(JSONObject.toJSONString(fields));
                testCase.setUpdateTime(System.currentTimeMillis());
                TestCaseExample example = new TestCaseExample();
                example.createCriteria().andIdEqualTo(testCase.getId());
                testCaseMapper.updateByExampleSelective(testCase, example);
            });
        } else {
            // 批量移动
            TestCaseWithBLOBs batchEdit = new TestCaseWithBLOBs();
            BeanUtils.copyBean(batchEdit, request);
            batchEdit.setUpdateTime(System.currentTimeMillis());
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(request.getIds());
            testCaseMapper.updateByExampleSelective(batchEdit, example);
        }
    }

    public void copyTestCaseBathPublic(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectPublicIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        TestCaseExample exampleList = new TestCaseExample();
        exampleList.createCriteria().andIdIn(request.getIds());
        List<TestCaseWithBLOBs> list = testCaseMapper.selectByExampleWithBLOBs(exampleList);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extTestCaseMapper::getLastOrder);
        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < list.size(); i++) {
                TestCaseWithBLOBs batchCopy = new TestCaseWithBLOBs();
                BeanUtils.copyBean(batchCopy, list.get(i));
                checkTestCaseExist(batchCopy);
                String oldTestCaseId = batchCopy.getId();
                String id = UUID.randomUUID().toString();
                batchCopy.setId(id);
                batchCopy.setName(ServiceUtils.getCopyName(batchCopy.getName()));
                batchCopy.setCreateTime(System.currentTimeMillis());
                batchCopy.setUpdateTime(System.currentTimeMillis());
                batchCopy.setCreateUser(SessionUtils.getUserId());
                batchCopy.setMaintainer(SessionUtils.getUserId());
                batchCopy.setReviewStatus(TestCaseReviewStatus.Prepare.name());
                batchCopy.setStatus(TestCaseReviewStatus.Prepare.name());
                batchCopy.setNodePath(request.getNodePath());
                batchCopy.setNodeId(request.getNodeId());
                batchCopy.setCasePublic(false);
                batchCopy.setRefId(id);
                if (!(batchCopy.getProjectId()).equals(SessionUtils.getCurrentProjectId())) {
                    String versionId = extProjectVersionMapper.getDefaultVersion(SessionUtils.getCurrentProjectId());
                    batchCopy.setVersionId(versionId);
                }
                batchCopy.setProjectId(SessionUtils.getCurrentProjectId());
                batchCopy.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                batchCopy.setCustomNum(String.valueOf(nextNum));
                batchCopy.setNum(nextNum++);
                mapper.insert(batchCopy);
                dealWithCopyOtherInfo(batchCopy, oldTestCaseId);
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void deleteTestCaseBath(TestCaseBatchRequest request) {
        TestCaseExample example = this.getBatchExample(request);
        //删除全部版本的数据根据 RefId
        List<String> refIds = testCaseMapper.selectByExample(example).stream().map(TestCase::getRefId).collect(Collectors.toList());
        example.clear();
        example.createCriteria().andRefIdIn(refIds);
        List<String> allVersionDataIds = testCaseMapper.selectByExample(example).stream().map(TestCase::getId).collect(Collectors.toList());
        request.setIds(allVersionDataIds);

        deleteTestPlanTestCaseBath(request.getIds());
        relationshipEdgeService.delete(request.getIds()); // 删除关系图

        request.getIds().forEach(testCaseId -> { // todo 优化下效率
            testCaseIssueService.delTestCaseIssues(testCaseId);
            testCaseCommentService.deleteCaseComment(testCaseId);
            TestCaseTestExample examples = new TestCaseTestExample();
            examples.createCriteria().andTestCaseIdEqualTo(testCaseId);
            testCaseTestMapper.deleteByExample(examples);
            relateDelete(testCaseId);
            deleteFollows(testCaseId);
        });

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
        if (testCase == null || testCase.getNum() == null) {
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

    public TestCase save(EditTestCaseRequest request, List<MultipartFile> files) {


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

        if (files != null) {
            files.forEach(file -> {
                final FileMetadata fileMetadata = fileService.saveFile(file, testCaseWithBLOBs.getProjectId());
                TestCaseFile testCaseFile = new TestCaseFile();
                testCaseFile.setCaseId(testCaseWithBLOBs.getId());
                testCaseFile.setFileId(fileMetadata.getId());
                testCaseFileMapper.insert(testCaseFile);
            });
        }

        return testCaseWithBLOBs;
    }

    public TestCase edit(EditTestCaseRequest request, List<MultipartFile> files) {
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(request.getId());
        request.setNum(testCaseWithBLOBs.getNum());
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
                final FileMetadata fileMetadata = fileService.saveFile(file, testCaseWithBLOBs.getProjectId());
                TestCaseFile testCaseFile = new TestCaseFile();
                testCaseFile.setFileId(fileMetadata.getId());
                testCaseFile.setCaseId(request.getId());
                testCaseFileMapper.insert(testCaseFile);
            });
        }
        this.setNode(request);
        return editTestCase(request);
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
        request.setStatus(null); // 不更新状态
        request.setRefId(testCaseWithBLOBs.getRefId());
        request.setVersionId(testCaseWithBLOBs.getVersionId());
        editTestCase(request);
        return request.getId();
    }

    public void minderEdit(TestCaseMinderEditRequest request) {

        deleteToGcBatch(request.getIds());

        testCaseNodeService.minderEdit(request);

        List<TestCaseMinderEditRequest.TestCaseMinderEditItem> data = request.getData();
        if (CollectionUtils.isNotEmpty(data)) {
            List<String> editIds = data.stream()
                    .filter(TestCaseMinderEditRequest.TestCaseMinderEditItem::getIsEdit)
                    .map(TestCaseWithBLOBs::getId).collect(Collectors.toList());

            Map<String, TestCaseWithBLOBs> testCaseMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(editIds)) {
                TestCaseExample example = new TestCaseExample();
                example.createCriteria().andIdIn(editIds);
                List<TestCaseWithBLOBs> testCaseWithBLOBs = testCaseMapper.selectByExampleWithBLOBs(example);
                testCaseMap = testCaseWithBLOBs.stream().collect(Collectors.toMap(TestCaseWithBLOBs::getId, t -> t));
            }

            for (TestCaseMinderEditRequest.TestCaseMinderEditItem item : data) {
                if (StringUtils.isBlank(item.getNodeId()) || item.getNodeId().equals("root")) {
                    item.setNodeId("");
                }
                item.setProjectId(request.getProjectId());
                if (item.getIsEdit()) {
                    TestCaseWithBLOBs dbCase = testCaseMap.get(item.getId());
                    if (editCustomFieldsPriority(dbCase, item.getPriority())) {
                        item.setCustomFields(dbCase.getCustomFields());
                    }
                    EditTestCaseRequest editRequest = new EditTestCaseRequest();
                    BeanUtils.copyBean(editRequest, item);
                    editTestCase(editRequest);
                    changeOrder(item, request.getProjectId());
                } else {
                    if (StringUtils.isBlank(item.getMaintainer())) {
                        item.setMaintainer(SessionUtils.getUserId());
                    }
                    EditTestCaseRequest editTestCaseRequest = new EditTestCaseRequest();
                    BeanUtils.copyBean(editTestCaseRequest, item);
                    addTestCase(editTestCaseRequest);
                    changeOrder(item, request.getProjectId());
                }
            }
        }

        minderExtraNodeService.batchEdit(request);
    }

    private void changeOrder(TestCaseMinderEditRequest.TestCaseMinderEditItem item, String projectId) {
        if (StringUtils.isNotBlank(item.getTargetId())) {
            ResetOrderRequest resetOrderRequest = new ResetOrderRequest();
            resetOrderRequest.setGroupId(projectId);
            resetOrderRequest.setMoveId(item.getId());
            resetOrderRequest.setTargetId(item.getTargetId());
            resetOrderRequest.setMoveMode(item.getMoveMode());
            updateOrder(resetOrderRequest);
        }
    }

    /**
     * 脑图编辑之后修改用例等级，同时修改自定义字段的用例等级
     *
     * @param dbCase
     * @param priority
     * @return
     */
    private boolean editCustomFieldsPriority(TestCaseWithBLOBs dbCase, String priority) {
        String customFields = dbCase.getCustomFields();
        if (StringUtils.isNotBlank(customFields)) {
            JSONArray fields = JSONObject.parseArray(customFields);
            for (int i = 0; i < fields.size(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.getString("name").equals("用例等级")) {
                    field.put("value", priority);
                    dbCase.setCustomFields(JSONObject.toJSONString(fields));
                    return true;
                }
            }
        }
        return false;
    }

    public List<TestCase> getTestCaseByProjectId(String projectId) {
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash").andLatestEqualTo(true);
        example.or().andProjectIdEqualTo(projectId).andStatusIsNull().andLatestEqualTo(true);
        return testCaseMapper.selectByExample(example);
    }

    public List<TestCaseDTO> listTestCaseForMinder(QueryTestCaseRequest request) {
        setDefaultOrder(request);
        List<TestCaseDTO> cases = extTestCaseMapper.listForMinder(request);
        List<String> caseIds = cases.stream().map(TestCaseDTO::getId).collect(Collectors.toList());
        HashMap<String, List<IssuesDao>> issueMap = buildMinderIssueMap(caseIds, IssueRefType.FUNCTIONAL.name());
        for (TestCaseDTO item : cases) {
            List<IssuesDao> issues = issueMap.get(item.getId());
            if (issues != null) {
                item.setIssueList(issues);
            }
        }
        return cases;
    }

    public HashMap<String, List<IssuesDao>> buildMinderIssueMap(List<String> caseIds, String refType) {
        HashMap<String, List<IssuesDao>> issueMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(caseIds)) {
            List<IssuesDao> issues = extIssuesMapper.getIssueForMinder(caseIds, refType);
            for (IssuesDao item : issues) {
                String key;
                if (item.getRefType().equals(IssueRefType.PLAN_FUNCTIONAL.name())) {
                    key = item.getRefId();
                } else {
                    key = item.getResourceId();
                }
                List<IssuesDao> list = issueMap.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(item);
                issueMap.put(key, list);
            }
        }
        return issueMap;
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
     *
     * @param projectId 项目ID
     */
    public void updateTestCaseCustomNumByProjectId(String projectId) {
        extTestCaseMapper.updateTestCaseCustomNumByProjectId(projectId);
    }

    public String getLogDetails(String id) {
        TestCaseWithBLOBs bloBs = testCaseMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, TestCaseReference.testCaseColumns);
            // 关联内容用例内容
            TestCaseTestExample example = new TestCaseTestExample();
            example.createCriteria().andTestCaseIdEqualTo(id);
            List<TestCaseTest> testCaseTests = testCaseTestMapper.selectByExample(example);
            StringBuilder nameBuilder = new StringBuilder();
            if (CollectionUtils.isNotEmpty(testCaseTests)) {
                List<String> testCaseIds = testCaseTests.stream()
                        .filter(user -> user.getTestType().equals("testcase")).map(TestCaseTest::getTestId)
                        .collect(Collectors.toList());

                List<String> performanceIds = testCaseTests.stream()
                        .filter(user -> user.getTestType().equals("performance")).map(TestCaseTest::getTestId)
                        .collect(Collectors.toList());

                List<String> automationIds = testCaseTests.stream()
                        .filter(user -> user.getTestType().equals("automation")).map(TestCaseTest::getTestId)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(testCaseIds)) {
                    ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
                    testCaseExample.createCriteria().andIdIn(testCaseIds);
                    List<ApiTestCase> testCases = apiTestCaseMapper.selectByExample(testCaseExample);
                    List<String> caseNames = testCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(caseNames)) {
                        nameBuilder.append("接口用例：").append("\n").append(caseNames).append("\n");
                    }
                }
                if (CollectionUtils.isNotEmpty(performanceIds)) {
                    LoadTestExample loadTestExample = new LoadTestExample();
                    loadTestExample.createCriteria().andIdIn(performanceIds);
                    List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
                    List<String> caseNames = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(caseNames)) {
                        nameBuilder.append("性能用例：").append("\n").append(caseNames).append("\n");
                    }
                }
                if (CollectionUtils.isNotEmpty(automationIds)) {
                    ApiScenarioExample scenarioExample = new ApiScenarioExample();
                    scenarioExample.createCriteria().andIdIn(automationIds);
                    List<ApiScenario> scenarios = apiScenarioMapper.selectByExample(scenarioExample);
                    List<String> caseNames = scenarios.stream().map(ApiScenario::getName).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(caseNames)) {
                        nameBuilder.append("自动化用例：").append("\n").append(caseNames).append("\n");
                    }
                }
            }
            DetailColumn column = new DetailColumn("关联测试", "testcase", nameBuilder.toString(), null);
            columns.add(column);

            //关联缺陷
            List<String> issuesNames = new LinkedList<>();
            TestCaseIssuesExample testCaseIssuesExample = new TestCaseIssuesExample();
            testCaseIssuesExample.createCriteria().andResourceIdEqualTo(bloBs.getId());
            List<TestCaseIssues> testCaseIssues = testCaseIssuesMapper.selectByExample(testCaseIssuesExample);
            if (CollectionUtils.isNotEmpty(testCaseIssues)) {
                List<String> issuesIds = testCaseIssues.stream().map(TestCaseIssues::getIssuesId).collect(Collectors.toList());
                IssuesExample issuesExample = new IssuesExample();
                issuesExample.createCriteria().andIdIn(issuesIds);
                List<Issues> issues = issuesMapper.selectByExample(issuesExample);
                if (CollectionUtils.isNotEmpty(issues)) {
                    issuesNames = issues.stream().map(Issues::getTitle).collect(Collectors.toList());
                }
            }
            DetailColumn issuesColumn = new DetailColumn("关联缺陷 ", "issues", String.join(",", issuesNames), null);
            columns.add(issuesColumn);
            //附件
            List<FileMetadata> originFiles = fileService.getFileMetadataByCaseId(id);
            List<String> fileNames = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(originFiles)) {
                fileNames = originFiles.stream().map(FileMetadata::getName).collect(Collectors.toList());
            }
            DetailColumn fileColumn = new DetailColumn("附件 ", "files", String.join(",", fileNames), null);
            columns.add(fileColumn);

            // 增加评论内容
            List<TestCaseCommentDTO> dtos = testCaseCommentService.getCaseComments(id);
            List<String> names = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(dtos)) {
                names = dtos.stream().map(TestCaseCommentDTO::getDescription).collect(Collectors.toList());
            }
            DetailColumn detailColumn = new DetailColumn("评论", "comment", String.join("\n", names), null);
            columns.add(detailColumn);

            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogBeforeDetails(String id) {
        TestPlanTestCaseWithBLOBs bloBs = testPlanTestCaseMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            String testCaseId = testPlanTestCaseMapper.selectByPrimaryKey(id).getCaseId();
            TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCaseId);
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, TestCaseReference.testCaseColumns);
            // 增加评论内容
            List<TestCaseCommentDTO> dtos = testCaseCommentService.getCaseComments(id);
            if (CollectionUtils.isNotEmpty(dtos)) {
                List<String> names = dtos.stream().map(TestCaseCommentDTO::getDescription).collect(Collectors.toList());
                DetailColumn detailColumn = new DetailColumn("评论", "comment", String.join("\n", names), null);
                columns.add(detailColumn);
            }
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(testCaseWithBLOBs.getId()), testCaseWithBLOBs.getProjectId(), testCaseWithBLOBs.getName(), testCaseWithBLOBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(ids);
            List<TestCase> definitions = testCaseMapper.selectByExample(example);
            List<String> names = definitions.stream().map(TestCase::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), definitions.get(0).getProjectId(), String.join(",", names), definitions.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public void reduction(TestCaseBatchRequest request) {
        TestCaseExample example = this.getBatchExample(request);
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            extTestCaseMapper.checkOriginalStatusByIds(request.getIds());

            //检查原来模块是否还在
            example = new TestCaseExample();
            // 关联版本之后，必须查询每一个数据的所有版本，依次还原
            example.createCriteria().andIdIn(request.getIds());
            List<TestCase> reductionCaseList = testCaseMapper.selectByExample(example);
            List<String> refIds = reductionCaseList.stream().map(TestCase::getRefId).collect(Collectors.toList());
            example.clear();
            example.createCriteria().andRefIdIn(refIds);
            reductionCaseList = testCaseMapper.selectByExample(example);
            request.setIds(reductionCaseList.stream().map(TestCase::getId).collect(Collectors.toList()));
            Map<String, List<TestCase>> nodeMap = reductionCaseList.stream().collect(Collectors.groupingBy(TestCase::getNodeId));
            for (Map.Entry<String, List<TestCase>> entry : nodeMap.entrySet()) {
                String nodeId = entry.getKey();
                long nodeCount = testCaseNodeService.countById(nodeId);
                if (nodeCount <= 0) {
                    String projectId = request.getProjectId();
                    TestCaseNode node = testCaseNodeService.getDefaultNode(projectId);
                    List<TestCase> testCaseList = entry.getValue();
                    for (TestCase testCase : testCaseList) {

                        TestCaseWithBLOBs updateCase = new TestCaseWithBLOBs();
                        updateCase.setId(testCase.getId());
                        updateCase.setNodeId(node.getId());
                        updateCase.setNodePath("/" + node.getName());

                        testCaseMapper.updateByPrimaryKeySelective(updateCase);
                    }
                }
            }
            extTestCaseMapper.reduction(request.getIds());
        }
    }

    public void deleteToGcBatch(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            for (String id : ids) {
                this.deleteTestCaseToGc(id);
            }
        }
    }

    public void deleteToGcBatchPublic(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            for (String id : ids) {
                TestCase testCase = testCaseMapper.selectByPrimaryKey(id);
                if ((StringUtils.isNotEmpty(testCase.getMaintainer()) && testCase.getMaintainer().equals(SessionUtils.getUserId())) ||
                        (StringUtils.isNotEmpty(testCase.getCreateUser()) && testCase.getCreateUser().equals(SessionUtils.getUserId()))) {
                    this.deleteTestCasePublic(null, testCase.getRefId());
                } else {
                    MSException.throwException(Translator.get("check_owner_case"));
                }
            }
        }
    }

    public String getCaseLogDetails(TestCaseMinderEditRequest request) {
        if (CollectionUtils.isNotEmpty(request.getData())) {
            List<String> ids = request.getData().stream().map(TestCase::getId).collect(Collectors.toList());
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(ids);
            List<TestCase> cases = testCaseMapper.selectByExample(example);
            List<String> names = cases.stream().map(TestCase::getName).collect(Collectors.toList());
            List<DetailColumn> columnsList = new LinkedList<>();
            DetailColumn column = new DetailColumn("名称", "name", String.join(",", names), null);
            columnsList.add(column);

            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), request.getProjectId(), String.join(",", names), SessionUtils.getUserId(), columnsList);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<ApiTestCaseDTO> getTestCaseApiCaseRelateList(ApiTestCaseRequest request) {
        List<ApiTestCaseDTO> apiTestCaseDTOS = testCaseTestMapper.relevanceApiList(request);
        ServiceUtils.buildVersionInfo(apiTestCaseDTOS);
        return apiTestCaseDTOS;
    }

    public List<ApiScenarioDTO> getTestCaseScenarioCaseRelateList(ApiScenarioRequest request) {
        List<ApiScenarioDTO> apiScenarioDTOS = testCaseTestMapper.relevanceScenarioList(request);
        ServiceUtils.buildVersionInfo(apiScenarioDTOS);
        return apiScenarioDTOS;
    }

    public List<LoadTestDTO> getTestCaseLoadCaseRelateList(LoadCaseRequest request) {
        List<LoadTestDTO> loadTestDTOS = testCaseTestMapper.relevanceLoadList(request);
        ServiceUtils.buildVersionInfo(loadTestDTOS);
        return loadTestDTOS;
    }

    public void relateTest(String type, String caseId, List<String> apiIds) {
        apiIds.forEach(testId -> {
            TestCaseTest testCaseTest = new TestCaseTest();
            testCaseTest.setTestType(type);
            testCaseTest.setTestCaseId(caseId);
            testCaseTest.setTestId(testId);
            testCaseTest.setCreateTime(System.currentTimeMillis());
            testCaseTest.setUpdateTime(System.currentTimeMillis());
            testCaseTestMapper.insert(testCaseTest);
        });
    }

    public void relateDelete(String caseId, String testId) {
        TestCaseTestExample example = new TestCaseTestExample();
        example.createCriteria()
                .andTestCaseIdEqualTo(caseId)
                .andTestIdEqualTo(testId);
        testCaseTestMapper.deleteByExample(example);
    }

    public void relateDelete(String caseId) {
        TestCaseTestExample example = new TestCaseTestExample();
        example.createCriteria()
                .andTestCaseIdEqualTo(caseId);
        testCaseTestMapper.deleteByExample(example);
    }

    public List<TestCaseTestDao> getRelateTest(String caseId) {
        TestCaseTestExample example = new TestCaseTestExample();
        example.createCriteria()
                .andTestCaseIdEqualTo(caseId);
        List<TestCaseTest> testCaseTests = testCaseTestMapper.selectByExample(example);
        Map<String, TestCaseTest> testCaseTestsMap = testCaseTests.stream()
                .collect(Collectors.toMap(TestCaseTest::getTestId, i -> i));
        List<ApiTestCase> apiCases = apiTestCaseService.getApiCaseByIds(
                getTestIds(testCaseTests, "testcase")
        );
        List<ApiScenario> apiScenarios = apiAutomationService.getScenarioCaseByIds(
                getTestIds(testCaseTests, "automation")
        );
        List<LoadTest> apiLoadTests = performanceTestService.getLoadCaseByIds(
                getTestIds(testCaseTests, "performance")
        );
        List<String> projectIds = apiCases.stream().map(c -> c.getProjectId()).collect(Collectors.toList());
        projectIds.addAll(apiScenarios.stream().map(s -> s.getProjectId()).collect(Collectors.toList()));
        projectIds.addAll(apiLoadTests.stream().map(s -> s.getProjectId()).collect(Collectors.toList()));
        projectIds = projectIds.stream().distinct().collect(Collectors.toList());

        List<String> versionIds = apiCases.stream().map(c -> c.getVersionId()).collect(Collectors.toList());
        versionIds.addAll(apiScenarios.stream().map(s -> s.getVersionId()).collect(Collectors.toList()));
        versionIds.addAll(apiLoadTests.stream().map(l -> l.getVersionId()).collect(Collectors.toList()));
        versionIds = versionIds.stream().distinct().collect(Collectors.toList());

        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projects = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            projects = projectMapper.selectByExample(projectExample);
        }

        Map<String, String> projectNameMap = projects.stream().collect(Collectors.toMap(Project::getId, Project::getName));

        ProjectVersionExample versionExample = new ProjectVersionExample();
        versionExample.createCriteria().andIdIn(versionIds);
        List<ProjectVersion> projectVersions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(versionIds)) {
            projectVersions = projectVersionMapper.selectByExample(versionExample);
        }

        Map<String, String> verisonNameMap = projectVersions.stream().collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));

        List<TestCaseTestDao> testCaseTestList = new ArrayList<>();
        apiCases.forEach(item -> {
            getTestCaseTestDaoList("testcase", item.getNum(), item.getName(), item.getId(), projectNameMap.get(item.getProjectId()), verisonNameMap.get(item.getVersionId()),
                    testCaseTestList, testCaseTestsMap);
        });
        apiScenarios.forEach(item -> {
            getTestCaseTestDaoList("automation", item.getNum(), item.getName(), item.getId(), projectNameMap.get(item.getProjectId()), verisonNameMap.get(item.getVersionId()),
                    testCaseTestList, testCaseTestsMap);
        });
        apiLoadTests.forEach(item -> {
            getTestCaseTestDaoList("performance", item.getNum(), item.getName(), item.getId(), projectNameMap.get(item.getProjectId()), verisonNameMap.get(item.getVersionId()),
                    testCaseTestList, testCaseTestsMap);
        });
        return testCaseTestList;
    }

    public void getTestCaseTestDaoList(String type, Object num, String name, String testId, String projectName, String versionName,
                                       List<TestCaseTestDao> testCaseTestList, Map<String, TestCaseTest> testCaseTestsMap) {
        TestCaseTestDao testCaseTestDao = new TestCaseTestDao();
        BeanUtils.copyBean(testCaseTestDao, testCaseTestsMap.get(testId));
        testCaseTestDao.setNum(num.toString());
        testCaseTestDao.setName(name);
        testCaseTestDao.setTestType(type);
        testCaseTestDao.setProjectName(projectName);
        testCaseTestDao.setVersionName(versionName);
        testCaseTestList.add(testCaseTestDao);
    }

    public List<String> getTestIds(List<TestCaseTest> testCaseTests, String type) {
        List<String> caseIds = testCaseTests.stream()
                .filter(item -> item.getTestType().equals(type))
                .map(TestCaseTest::getTestId)
                .collect(Collectors.toList());
        return caseIds;
    }

    public TestCaseWithBLOBs getTestCaseStep(String testCaseId) {
        return extTestCaseMapper.getTestCaseStep(testCaseId);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestCaseWithBLOBs.class, TestCaseMapper.class,
                extTestCaseMapper::selectProjectIds,
                extTestCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestCaseWithBLOBs.class,
                testCaseMapper::selectByPrimaryKey,
                extTestCaseMapper::getPreOrder,
                extTestCaseMapper::getLastOrder,
                testCaseMapper::updateByPrimaryKeySelective);
    }

    public Pager<List<TestCaseDTO>> getRelationshipRelateList(QueryTestCaseRequest request, int goPage, int pageSize) {
        setDefaultOrder(request);
        List<String> relationshipIds = relationshipEdgeService.getRelationshipIds(request.getId());
        request.setTestCaseContainIds(relationshipIds);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, extTestCaseMapper.getTestCase(request));
    }

    public List<RelationshipEdgeDTO> getRelationshipCase(String id, String relationshipType) {

        List<RelationshipEdge> relationshipEdges = relationshipEdgeService.getRelationshipEdgeByType(id, relationshipType);
        List<String> ids = relationshipEdgeService.getRelationIdsByType(relationshipType, relationshipEdges);

        if (CollectionUtils.isNotEmpty(ids)) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(ids).andStatusNotEqualTo("Trash");
            List<TestCaseWithBLOBs> testCaseList = testCaseMapper.selectByExampleWithBLOBs(example);

            Map<String, String> userNameMap = ServiceUtils.getUserNameMap(testCaseList.stream().map(TestCaseWithBLOBs::getCreateUser).collect(Collectors.toList()));

            Map<String, String> verionNameMap = new HashMap<>();
            List<String> versionIds = testCaseList.stream().map(TestCase::getVersionId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(versionIds)) {
                ProjectVersionRequest pvr = new ProjectVersionRequest();
                pvr.setProjectId(testCaseList.get(0).getProjectId());
                List<ProjectVersionDTO> projectVersions = extProjectVersionMapper.selectProjectVersionList(pvr);
                verionNameMap = projectVersions.stream().collect(Collectors.toMap(ProjectVersionDTO::getId, ProjectVersionDTO::getName));
            }
            Map<String, TestCase> caseMap = testCaseList.stream().collect(Collectors.toMap(TestCase::getId, i -> i));
            List<RelationshipEdgeDTO> results = new ArrayList<>();
            for (RelationshipEdge relationshipEdge : relationshipEdges) {
                RelationshipEdgeDTO relationshipEdgeDTO = new RelationshipEdgeDTO();
                BeanUtils.copyBean(relationshipEdgeDTO, relationshipEdge);
                TestCase testCase;
                if (StringUtils.equals(relationshipType, "PRE")) {
                    testCase = caseMap.get(relationshipEdge.getTargetId());
                } else {
                    testCase = caseMap.get(relationshipEdge.getSourceId());
                }
                if (testCase == null) {
                    continue; // 用例可能在回收站
                }
                relationshipEdgeDTO.setTargetName(testCase.getName());
                relationshipEdgeDTO.setCreator(userNameMap.get(testCase.getCreateUser()));
                relationshipEdgeDTO.setTargetNum(testCase.getNum());
                relationshipEdgeDTO.setTargetCustomNum(testCase.getCustomNum());
                relationshipEdgeDTO.setStatus(testCase.getStatus());
                relationshipEdgeDTO.setVersionName(verionNameMap.get(testCase.getVersionId()));
                results.add(relationshipEdgeDTO);
            }
            return results;
        }
        return new ArrayList<>();
    }

    public void buildProjectInfo(String projectId, List<TestCaseDTO> list) {
        Project project = projectService.getProjectById(projectId);
        list.forEach(item -> {
            item.setProjectName(project.getName());
        });
    }

    public void buildUserInfo(List<TestCaseDTO> testCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(testCases.stream().map(TestCase::getCreateUser).collect(Collectors.toList()));
        userIds.addAll(testCases.stream().map(TestCase::getDeleteUserId).collect(Collectors.toList()));
        userIds.addAll(testCases.stream().map(TestCase::getMaintainer).collect(Collectors.toList()));
        if (!org.apache.commons.collections.CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            testCases.forEach(caseResult -> {
                caseResult.setCreateName(userMap.get(caseResult.getCreateUser()));
                caseResult.setDeleteUserId(userMap.get(caseResult.getDeleteUserId()));
                caseResult.setMaintainerName(userMap.get(caseResult.getMaintainer()));
            });
        }
    }

    public int getRelationshipCount(String id) {
        return relationshipEdgeService.getRelationshipCount(id, extTestCaseMapper::countByIds);
    }

    public List<String> getFollows(String caseId) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(caseId)) {
            return result;
        }
        TestCaseFollowExample example = new TestCaseFollowExample();
        example.createCriteria().andCaseIdEqualTo(caseId);
        List<TestCaseFollow> follows = testCaseFollowMapper.selectByExample(example);
        return follows.stream().map(TestCaseFollow::getFollowId).distinct().collect(Collectors.toList());
    }

    public List<TestCaseWithBLOBs> getTestCasesWithBLOBs(List<String> ids) {
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdIn(ids);
        return testCaseMapper.selectByExampleWithBLOBs(example);
    }

    public void copyTestCaseBath(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        List<TestCaseWithBLOBs> testCases = getTestCasesWithBLOBs(ids);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extTestCaseMapper::getLastOrder);

        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < testCases.size(); i++) {
                TestCaseWithBLOBs testCase = testCases.get(i);
                String id = UUID.randomUUID().toString();
                String oldTestCaseId = testCase.getId();
                testCase.setId(id);
                testCase.setName(ServiceUtils.getCopyName(testCase.getName()));
                testCase.setNodeId(request.getNodeId());
                testCase.setNodePath(request.getNodePath());
                testCase.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                testCase.setCustomNum(String.valueOf(nextNum));
                testCase.setNum(nextNum++);
                testCase.setCasePublic(false);
                testCase.setCreateTime(System.currentTimeMillis());
                testCase.setUpdateTime(System.currentTimeMillis());
                testCase.setRefId(id);
                mapper.insert(testCase);

                dealWithCopyOtherInfo(testCase, oldTestCaseId);
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<TestCaseDTO> getTestCaseVersions(String caseId) {
        TestCaseWithBLOBs testCase = testCaseMapper.selectByPrimaryKey(caseId);
        if (testCase == null) {
            return new ArrayList<>();
        }
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setRefId(testCase.getRefId());
        if (ScenarioStatus.Trash.name().equalsIgnoreCase(testCase.getStatus())) {
            request.setFilters(new HashMap<String, List<String>>() {{
                put("status", new ArrayList() {{
                    add(ScenarioStatus.Trash.name());
                }});
            }});
        }
        return this.listTestCase(request);
    }

    public TestCaseDTO getTestCaseByVersion(String refId, String version) {
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setRefId(refId);
        request.setVersionId(version);
        List<TestCaseDTO> testCaseList = this.listTestCase(request);
        if (CollectionUtils.isEmpty(testCaseList)) {
            return null;
        }
        return testCaseList.get(0);
    }

    public void deleteTestCaseByVersion(String refId, String version) {
        TestCaseExample e = new TestCaseExample();
        e.createCriteria().andRefIdEqualTo(refId).andVersionIdEqualTo(version);
        List<TestCaseWithBLOBs> testCaseList = testCaseMapper.selectByExampleWithBLOBs(e);
        if (CollectionUtils.isNotEmpty(testCaseList)) {
            testCaseMapper.deleteByExample(e);
            //检查最新版本
            checkAndSetLatestVersion(refId, version, testCaseList.get(0).getProjectId(), "del");
        }
    }

    /**
     * 检查设置最新版本
     *
     * @param refId
     * @param versionId
     */
    private void checkAndSetLatestVersion(String refId, String versionId, String projectId, String opt) {
        if (StringUtils.isAnyBlank(refId, versionId, projectId))
            return;
        if (StringUtils.equalsAnyIgnoreCase("add", opt)) {
            String latestVersion = extProjectVersionMapper.getDefaultVersion(projectId);
            if (StringUtils.equals(versionId, latestVersion)) {
                TestCaseExample e = new TestCaseExample();
                e.createCriteria().andRefIdEqualTo(refId).andVersionIdEqualTo(versionId);
                List<String> changeToLatestIds = testCaseMapper.selectByExample(e).stream().map(TestCase::getId).collect(Collectors.toList());
                //如果是最新版本
                TestCaseWithBLOBs t = new TestCaseWithBLOBs();
                t.setLatest(true);
                testCaseMapper.updateByExampleSelective(t, e);

                if (CollectionUtils.isNotEmpty(changeToLatestIds)) {
                    e.clear();
                    e.createCriteria().andRefIdEqualTo(refId).andIdNotIn(changeToLatestIds);
                    t.setLatest(false);
                    testCaseMapper.updateByExampleSelective(t, e);
                }
            }
        } else if (StringUtils.equalsIgnoreCase("del", opt)) {
            //删掉该版本数据
            TestCaseExample e = new TestCaseExample();
            e.createCriteria().andRefIdEqualTo(refId).andLatestEqualTo(true);
            if (testCaseMapper.countByExample(e) == 0) {
                //删掉最新版本的数据
                e.clear();
                e.createCriteria().andRefIdEqualTo(refId).andVersionIdNotEqualTo(versionId);
                List<TestCase> differentVersionData = testCaseMapper.selectByExample(e);
                if (differentVersionData.size() != 0) {
                    ProjectVersionExample pe = new ProjectVersionExample();
                    pe.createCriteria().andProjectIdEqualTo(projectId);
                    pe.setOrderByClause("create_time desc");
                    List<ProjectVersion> versionList = projectVersionMapper.selectByExample(pe);
                    TestCase latestData = null;
                    for (ProjectVersion projectVersion : versionList) {
                        for (TestCase t : differentVersionData)
                            if (projectVersion.getId().equalsIgnoreCase(t.getVersionId())) {
                                latestData = t;
                                break;
                            }
                        if (latestData != null)
                            break;
                    }
                    if (latestData == null)
                        latestData = differentVersionData.get(0);
                    latestData.setLatest(true);
                    testCaseMapper.updateByPrimaryKey(latestData);
                }
            }
        }

    }

    public void deleteTestCasePublic(String versionId, String refId) {
        TestCase testCase = new TestCase();
        testCase.setRefId(refId);
        testCase.setVersionId(versionId);
        extTestCaseMapper.deletePublic(testCase);
    }

    public Boolean hasOtherInfo(String caseId) {
        TestCaseWithBLOBs tc = getTestCase(caseId);
        if (tc != null) {
            if (StringUtils.isNotBlank(tc.getRemark()) || StringUtils.isNotBlank(tc.getDemandId()) || CollectionUtils.isNotEmpty(getRelateTest(caseId))
                    || CollectionUtils.isNotEmpty(issuesService.getIssues(caseId, IssueRefType.FUNCTIONAL.name())) || CollectionUtils.isNotEmpty(getRelationshipCase(caseId, "PRE")) || CollectionUtils.isNotEmpty(getRelationshipCase(caseId, "POST"))
                    || CollectionUtils.isNotEmpty(fileService.getFileMetadataByCaseId(caseId))) {
                return true;
            }
        }
        return false;
    }
}
