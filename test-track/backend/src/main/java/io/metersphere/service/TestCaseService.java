package io.metersphere.service;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.AttachmentType;
import io.metersphere.constants.TestCaseTestType;
import io.metersphere.dto.*;
import io.metersphere.excel.constants.TestCaseImportFiled;
import io.metersphere.excel.converter.TestCaseExportConverter;
import io.metersphere.excel.converter.TestCaseExportConverterFactory;
import io.metersphere.excel.domain.*;
import io.metersphere.excel.handler.FunctionCaseMergeWriteHandler;
import io.metersphere.excel.handler.FunctionCaseTemplateWriteHandler;
import io.metersphere.excel.listener.TestCaseNoModelDataListener;
import io.metersphere.excel.listener.TestCasePretreatmentListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.excel.utils.ExcelImportType;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestCaseReference;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.TestPlanTestCaseService;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ProjectVersionRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.remote.ui.RelevanceUiCaseService;
import io.metersphere.xpack.track.dto.AttachmentRequest;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.request.testcase.*;
import io.metersphere.service.issue.platform.IssueFactory;
import io.metersphere.service.remote.api.RelevanceApiCaseService;
import io.metersphere.service.remote.performance.RelevanceLoadCaseService;
import io.metersphere.service.remote.project.TrackTestCaseTemplateService;
import io.metersphere.service.wapper.TrackProjectService;
import io.metersphere.utils.DiscoveryUtil;
import io.metersphere.xmind.XmindCaseParser;
import io.metersphere.xmind.pojo.TestCaseXmindData;
import io.metersphere.xmind.utils.XmindExportUtil;
import io.metersphere.xpack.track.dto.EditTestCaseRequest;
import io.metersphere.xpack.track.dto.IssuesDao;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.issue.IssuesPlatform;
import io.metersphere.xpack.version.service.ProjectVersionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    BaseUserService baseUserService;

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;

    @Resource
    ProjectMapper projectMapper;
    @Resource
    TrackProjectService trackProjectService;
    @Resource
    BaseProjectService baseProjectService;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    TestCaseNodeService testCaseNodeService;

    //    @Resource
    //    ApiTestCaseMapper apiTestCaseMapper;

    @Resource
    TestCaseIssueService testCaseIssueService;
    @Resource
    io.metersphere.service.TestCaseCommentService testCaseCommentService;
    @Resource
    AttachmentService attachmentService;
    @Resource
    FileService fileService;
    @Resource
    TestCaseFileMapper testCaseFileMapper;
    @Resource
    TestCaseTestMapper testCaseTestMapper;
    @Resource
    AttachmentModuleRelationMapper attachmentModuleRelationMapper;
    //    @Resource
    //    private LoadTestMapper loadTestMapper;
    //    @Resource
    //    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestCaseIssuesMapper testCaseIssuesMapper;
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private RelationshipEdgeService relationshipEdgeService;
    @Resource
    private RelevanceApiCaseService relevanceApiCaseService;
    @Resource
    private RelevanceLoadCaseService relevanceLoadCaseService;
    @Resource
    private RelevanceUiCaseService relevanceUiCaseService;
    //    @Resource
    //    @Lazy
    //    private ApiTestCaseService apiTestCaseService;
    //    @Resource
    //    @Lazy
    //    private ApiAutomationService apiAutomationService;
    //    @Resource
    //    @Lazy
    //    private PerformanceTestService performanceTestService;
    @Resource
    private TestCaseFollowMapper testCaseFollowMapper;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private MinderExtraNodeService minderExtraNodeService;
    @Resource
    private IssuesService issuesService;
    @Resource
    private RelationshipEdgeMapper relationshipEdgeMapper;
    @Resource
    private BaseProjectVersionMapper baseProjectVersionMapper;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Lazy
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private CustomFieldTestCaseMapper customFieldTestCaseMapper;
    @Resource
    private CustomFieldTestCaseService customFieldTestCaseService;
    @Resource
    private FunctionCaseExecutionInfoService functionCaseExecutionInfoService;
    @Lazy
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Lazy
    @Resource
    private TestReviewTestCaseService testReviewTestCaseService;
    @Resource
    private FileAttachmentMetadataMapper fileAttachmentMetadataMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    @Lazy
    private TrackTestCaseTemplateService trackTestCaseTemplateService;
    @Resource
    BaseCustomFieldService baseCustomFieldService;
    @Resource
    private ExtTestCaseCountMapper extTestCaseCountMapper;
    @Resource
    private ExtTestAnalysisMapper extTestAnalysisMapper;

    private ThreadLocal<Integer> importCreateNum = new ThreadLocal<>();

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
        request.setDemandId(request.getDemandId());
        request.setDemandName(request.getDemandName());
        request.setCreateUser(SessionUtils.getUserId());
        this.setNode(request);
        request.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extTestCaseMapper::getLastOrder));
        //直接点保存 || 复制走的逻辑
        if (StringUtils.isAllBlank(request.getRefId(), request.getVersionId())) {
            //新创建测试用例，默认使用最新版本
            request.setRefId(request.getId());
            request.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        } else if (StringUtils.isBlank(request.getRefId()) && StringUtils.isNotBlank(request.getVersionId())) {
            //从版本选择直接创建
            request.setRefId(request.getId());
        }
        //完全新增一条记录直接就是最新
        request.setLatest(true);

        // 同步用例与需求的关联关系
        addDemandHyperLink(request, "add");

        testCaseMapper.insert(request);
        saveFollows(request.getId(), request.getFollows());
        List<CustomFieldResourceDTO> addFields = request.getAddFields();
        if (CollectionUtils.isNotEmpty(addFields)) {
            addFields.addAll(request.getEditFields());
            customFieldTestCaseService.addFields(request.getId(), addFields);
        }
        return request;
    }

    public void addDemandHyperLink(EditTestCaseRequest request, String type) {
        IssuesRequest updateRequest = getIssuesRequest(request);
        Project project = baseProjectService.getProjectById(request.getProjectId());
        if (StringUtils.equals(project.getPlatform(), IssuesManagePlatform.AzureDevops.name())) {
            doAddDemandHyperLink(request, type, updateRequest, project);
        }
    }

    private void doAddDemandHyperLink(EditTestCaseRequest request, String type, IssuesRequest updateRequest, Project project) {
        updateRequest.setWorkspaceId(project.getWorkspaceId());
        List<IssuesPlatform> platformList = getAddPlatforms(updateRequest);
        platformList.forEach(platform -> {
            platform.updateDemandHyperLink(request, project, type);
        });
    }

    private IssuesRequest getIssuesRequest(EditTestCaseRequest request) {
        IssuesRequest updateRequest = new IssuesRequest();
        updateRequest.setId(request.getId());
        updateRequest.setResourceId(request.getDemandId());
        updateRequest.setProjectId(request.getProjectId());
        updateRequest.setTestCaseId(request.getId());
        return updateRequest;
    }

    public void addDemandHyperLinkBatch(List<String> testcaseIds, String projectId) {
        if (CollectionUtils.isEmpty(testcaseIds)) {
            return;
        }

        Project project;
        if (StringUtils.isNotBlank(projectId)) {
            project = baseProjectService.getProjectById(projectId);
        } else {
            TestCaseWithBLOBs testCase = testCaseMapper.selectByPrimaryKey(testcaseIds.get(0));
            // 同步删除用例与需求的关联关系
            project = baseProjectService.getProjectById(testCase.getProjectId());
        }

        // AzureDevops 才处理
        if (StringUtils.equals(project.getPlatform(), IssuesManagePlatform.AzureDevops.name())) {
            testcaseIds.forEach(id -> {
                TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(id);
                if (testCaseWithBLOBs != null) {
                    EditTestCaseRequest request = new EditTestCaseRequest();
                    BeanUtils.copyBean(request, testCaseWithBLOBs);
                    addDemandHyperLink(request, "delete");
                }
            });
        }
    }

    private List<IssuesPlatform> getAddPlatforms(IssuesRequest request) {
        List<String> platforms = new ArrayList<>();
        // 缺陷管理关联
        platforms.add(issuesService.getPlatform(request.getProjectId()));

        if (CollectionUtils.isEmpty(platforms)) {
            platforms.add(IssuesManagePlatform.Local.toString());
        }
        return IssueFactory.createPlatforms(platforms, request);
    }

    private void dealWithCopyOtherInfo(TestCaseWithBLOBs testCase, String oldTestCaseId) {
        EditTestCaseRequest request = new EditTestCaseRequest();
        BeanUtils.copyBean(request, testCase);
        EditTestCaseRequest.OtherInfoConfig otherInfoConfig = EditTestCaseRequest.OtherInfoConfig.createDefault();
        request.setOtherInfoConfig(otherInfoConfig);
        dealWithOtherInfoOfNewVersion(request, oldTestCaseId);
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
            Project project = baseProjectService.getProjectById(projectId);
            if (project != null) {
                ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
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
                .andStatusNotEqualTo(CommonConstants.TrashStatus)
                .andIdNotEqualTo(testCase.getId());
        if (testCaseWithBLOBs != null && StringUtils.isNotBlank(testCaseWithBLOBs.getRefId())) {
            criteria.andRefIdNotEqualTo(testCaseWithBLOBs.getRefId());
        }
        List<TestCase> list = testCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            MSException.throwException(Translator.get("custom_num_is_exist"));
        }
    }

    public TestCaseDTO getTestCase(String testCaseId) {
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCaseId);
        TestCaseDTO testCaseDTO = new TestCaseDTO();
        BeanUtils.copyBean(testCaseDTO, testCaseWithBLOBs);
        buildCustomField(testCaseDTO);
        return testCaseDTO;
    }

    public TestCaseWithBLOBs editTestCase(EditTestCaseRequest testCase) {
        checkTestCustomNum(testCase);
        testCase.setUpdateTime(System.currentTimeMillis());
        TestCaseWithBLOBs originCase = testCaseMapper.selectByPrimaryKey(testCase.getId());

        try {
            // 同步缺陷与需求的关联关系
            updateThirdPartyIssuesLink(testCase);

            // 同步用例与需求的关联关系
            addDemandHyperLink(testCase, "edit");
        } catch (Exception e) {
            LogUtil.error(e);
        }

        if (StringUtils.isEmpty(testCase.getDemandId())) {
            testCase.setDemandId(StringUtils.EMPTY);
        }
        createNewVersionOrNot(testCase);

        if (StringUtils.isNotBlank(testCase.getCustomNum()) && StringUtils.isNotBlank(testCase.getId())) {
            TestCaseWithBLOBs caseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCase.getId());
            if (caseWithBLOBs != null) {
                TestCaseExample example = new TestCaseExample();
                example.createCriteria().andRefIdEqualTo(caseWithBLOBs.getRefId());
                TestCaseWithBLOBs testCaseWithBLOBs = new TestCaseWithBLOBs();
                testCaseWithBLOBs.setCustomNum(testCase.getCustomNum());
                testCaseMapper.updateByExampleSelective(testCaseWithBLOBs, example);
            }
        }

        customFieldTestCaseService.editFields(testCase.getId(), testCase.getEditFields());
        customFieldTestCaseService.addFields(testCase.getId(), testCase.getAddFields());

        // latest 字段 createNewVersionOrNot 已经设置过了，不更新
        testCase.setLatest(null);

        testCaseMapper.updateByPrimaryKeySelective(testCase);

        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCase.getId());

        reReviewTestReviewTestCase(originCase, testCaseWithBLOBs);

        return testCaseWithBLOBs;
    }

    /**
     * 如果启用重新提审，并且前置条件或步骤发生变化，则触发重新提审
     */
    private void reReviewTestReviewTestCase(TestCaseWithBLOBs originCase, TestCaseWithBLOBs testCase) {
        ProjectConfig config = baseProjectApplicationService.getProjectConfig(testCase.getProjectId());
        Boolean reReview = config.getReReview();
        if (BooleanUtils.isTrue(reReview) && originCase != null) {
            if (!StringUtils.equals(originCase.getPrerequisite(), testCase.getPrerequisite())   // 前置条件添加发生变化
            || !StringUtils.equals(originCase.getSteps(), testCase.getSteps())                  // 步骤发生变化
            || !StringUtils.equals(originCase.getStepDescription(), testCase.getStepDescription())
            || !StringUtils.equals(originCase.getExpectedResult(), testCase.getExpectedResult())) {
                testReviewTestCaseService.reReviewByCaseId(testCase.getId());
            }
        }
    }

    /**
     * 判断azure devops用例关联的需求是否发生变更，若发生变更，则重新建立需求与缺陷的关联关系
     *
     * @param testCase
     */
    public void updateThirdPartyIssuesLink(EditTestCaseRequest testCase) {
        if (StringUtils.isBlank(testCase.getProjectId())) {
            return;
        }
        Project project = baseProjectService.getProjectById(testCase.getProjectId());
        if (!StringUtils.equals(project.getPlatform(), IssuesManagePlatform.AzureDevops.name())) {
            return;
        }
        doUpdateThirdPartyIssuesLink(testCase, project);
    }

    private void doUpdateThirdPartyIssuesLink(EditTestCaseRequest testCase, Project project) {
        IssuesRequest issuesRequest = new IssuesRequest();
        if (!issuesService.isThirdPartTemplate(project)) {
            issuesRequest.setDefaultCustomFields(issuesService.getDefaultCustomFields(testCase.getProjectId()));
        }
        issuesRequest.setProjectId(testCase.getProjectId());
        issuesRequest.setWorkspaceId(project.getWorkspaceId());
        IssuesPlatform platform = IssueFactory.createPlatform(issuesService.getPlatform(testCase.getProjectId()), issuesRequest);
        if (platform != null) {
            platform.updateDemandIssueLink(testCase, project);
        }
    }

    /**
     * 根据前后端 versionId 判定是编辑旧数据还是创建新版本
     *
     * @param testCase
     */
    private void createNewVersionOrNot(EditTestCaseRequest testCase) {
        if (StringUtils.isBlank(testCase.getVersionId())) {
            return;
        }
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdEqualTo(testCase.getId())
                .andVersionIdEqualTo(testCase.getVersionId());

        String defaultVersion = baseProjectVersionMapper.getDefaultVersion(testCase.getProjectId());
        if (StringUtils.equalsIgnoreCase(testCase.getVersionId(), defaultVersion)) {
            testCase.setLatest(false);
        }

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
            dealWithOtherInfoOfNewVersion(testCase, oldTestCase.getId());
            testCaseMapper.insertSelective(testCase);
        }

        if (StringUtils.equalsIgnoreCase(testCase.getVersionId(), defaultVersion)) {
            checkAndSetLatestVersion(testCase.getRefId());
        }
        if (StringUtils.isNotBlank(testCase.getNodePath())) {
            //同步修改所有版本的模块路径
            updateOtherVersionModule(testCase);
        }
    }

    private void updateOtherVersionModule(EditTestCaseRequest testCase) {
        extTestCaseMapper.updateVersionModule(testCase.getRefId(), testCase.getVersionId(), testCase.getNodeId(), testCase.getNodePath());
    }

    /**
     * 处理其他信息的复制问题
     *
     * @param testCase
     * @param oldTestCaseId
     */
    private void dealWithOtherInfoOfNewVersion(EditTestCaseRequest testCase, String oldTestCaseId) {
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
                        t.setRelateTime(System.currentTimeMillis());
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
                AttachmentRequest attachmentRequest = new AttachmentRequest();
                attachmentRequest.setBelongId(testCase.getId());
                attachmentRequest.setCopyBelongId(oldTestCaseId);
                attachmentRequest.setBelongType(AttachmentType.TEST_CASE.type());
                attachmentService.copyAttachment(attachmentRequest);
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
                    .andTypeEqualTo(testCase.getType())
                    .andStatusNotEqualTo("Trash");
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
                String caseRemark = testCase.getRemark() == null ? StringUtils.EMPTY : testCase.getRemark();
                String caseSteps = testCase.getSteps() == null ? StringUtils.EMPTY : testCase.getSteps();
                String casePrerequisite = testCase.getPrerequisite() == null ? StringUtils.EMPTY : testCase.getPrerequisite();
                for (TestCaseWithBLOBs tc : caseList) {
                    String steps = tc.getSteps() == null ? StringUtils.EMPTY : tc.getSteps();
                    String remark = tc.getRemark() == null ? StringUtils.EMPTY : tc.getRemark();
                    String prerequisite = tc.getPrerequisite() == null ? StringUtils.EMPTY : tc.getPrerequisite();
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
     * 根据id和projectId查询id是否在数据库中存在。
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
        customFieldTestCaseService.deleteByResourceId(testCaseId); // 删除自定义字段关联关系
        functionCaseExecutionInfoService.deleteBySourceId(testCaseId);
        // 删除用例附件关联数据, 附件内容
        AttachmentRequest request = new AttachmentRequest();
        request.setBelongId(testCaseId);
        request.setBelongType(AttachmentType.TEST_CASE.type());
        attachmentService.deleteAttachment(request);
        return testCaseMapper.deleteByPrimaryKey(testCaseId);
    }

    public int deleteTestCaseTestByTestId(String testId) {
        return deleteTestCaseTestByTestIds(Arrays.asList(testId));
    }

    public int deleteTestCaseTestByTestIds(List<String> testIds) {
        TestCaseTestExample examples = new TestCaseTestExample();
        examples.createCriteria().andTestIdIn(testIds);
        return testCaseTestMapper.deleteByExample(examples);
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

        // 同步删除用例与需求的关联关系
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCaseId);
        if (testCaseWithBLOBs != null) {
            EditTestCaseRequest request = new EditTestCaseRequest();
            BeanUtils.copyBean(request, testCaseWithBLOBs);
            addDemandHyperLink(request, "delete");
        }

        DeleteTestCaseRequest request = new DeleteTestCaseRequest();
        BeanUtils.copyBean(request, testCase);
        testPlanTestCaseService.deleteToGc(Arrays.asList(testCaseId));
        testReviewTestCaseService.deleteToGc(Arrays.asList(testCaseId));
        return extTestCaseMapper.deleteToGc(request);
    }

    public int deleteToGcBatch(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extTestCaseMapper.selectIds(query));
        return deleteToGcBatch(request.getIds(), null);
    }

    public int deleteToGcBatch(List<String> ids, String projectId) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        TestCase testCase = new TestCase();
        testCase.setDeleteUserId(SessionUtils.getUserId());
        testCase.setDeleteTime(System.currentTimeMillis());

        addDemandHyperLinkBatch(ids, projectId);

        DeleteTestCaseRequest request = new DeleteTestCaseRequest();
        BeanUtils.copyBean(request, testCase);
        request.setIds(ids);
        testPlanTestCaseService.deleteToGc(ids);
        testReviewTestCaseService.deleteToGc(ids);
        return extTestCaseMapper.deleteToGc(request);
    }

    public List<TestCaseDTO> listTestCase(QueryTestCaseRequest request) {
        return listTestCase(request, false);
    }

    public List<TestCaseDTO> listTestCase(QueryTestCaseRequest request, boolean isSampleInfo) {
        this.initRequest(request, true);
        setDefaultOrder(request);
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        if (request.getFilters() != null && !request.getFilters().containsKey("status")) {
            request.getFilters().put("status", new ArrayList<>(0));
        }
        List<TestCaseDTO> list = extTestCaseMapper.list(request);
        if (!isSampleInfo) {
            buildUserInfo(list);
            if (StringUtils.isNotBlank(request.getProjectId())) {
                buildProjectInfo(request.getProjectId(), list);
            } else {
                buildProjectInfoWithoutProject(list);
            }
            buildCustomField(list);
        }
        return list;
    }

    private void buildCustomField(List<TestCaseDTO> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        Map<String, List<CustomFieldDao>> fieldMap =
                customFieldTestCaseService.getMapByResourceIds(data.stream().map(TestCaseDTO::getId).collect(Collectors.toList()));
        data.forEach(i -> i.setFields(fieldMap.get(i.getId())));
    }

    private void buildCustomField(TestCaseDTO data) {
        data.setFields(getCustomFieldByCaseId(data.getId()));
    }

    public List<CustomFieldDao> getCustomFieldByCaseId(String caseId) {
        CustomFieldTestCaseExample example = new CustomFieldTestCaseExample();
        example.createCriteria().andResourceIdEqualTo(caseId);
        List<CustomFieldTestCase> customFieldTestCases = customFieldTestCaseMapper.selectByExampleWithBLOBs(example);
        List<CustomFieldDao> fields = new ArrayList<>();
        customFieldTestCases.forEach(i -> {
            CustomFieldDao customFieldDao = new CustomFieldDao();
            customFieldDao.setId(i.getFieldId());
            customFieldDao.setValue(i.getValue());
            customFieldDao.setTextValue(i.getTextValue());
            fields.add(customFieldDao);
        });
        return fields;
    }

    private void buildProjectInfoWithoutProject(List<TestCaseDTO> resList) {
        resList.forEach(i -> {
            Project project = projectMapper.selectByPrimaryKey(i.getProjectId());
            i.setProjectName(project.getName());
        });
    }

    public void setPublicListRequestParam(QueryTestCaseRequest request) {
        this.initRequest(request, true);
        setDefaultOrder(request);
        if (request.getFilters() != null && !request.getFilters().containsKey("status")) {
            request.getFilters().put("status", new ArrayList<>(0));
        }
        if (StringUtils.isBlank(request.getWorkspaceId())) {
            MSException.throwException("workspaceId could not be null!");
        }
        request.setProjectId(null);
        // 保留: 后续若需要根据列表版本筛选的话, version_id => version_name
        List<String> versionIds = request.getFilters().get("version_id");
        if (CollectionUtils.isNotEmpty(versionIds)) {
            ProjectVersionExample versionExample = new ProjectVersionExample();
            versionExample.createCriteria().andIdIn(versionIds);
            List<ProjectVersion> versions = projectVersionMapper.selectByExample(versionExample);
            List<String> versionNames = versions.stream().map(ProjectVersion::getName).distinct().collect(Collectors.toList());
            request.getFilters().put("version_name", versionNames);
            request.getFilters().put("version_id", Collections.emptyList());
        }
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
    }

    public List<TestCaseDTO> publicListTestCase(QueryTestCaseRequest request) {
        List<TestCaseDTO> returnList = extTestCaseMapper.publicList(request);
        ServiceUtils.buildProjectInfo(returnList);
        buildUserInfo(returnList);
        buildVersionInfo(returnList);
        buildPublicCustomField(request, returnList);
        return returnList;
    }

    private void buildPublicCustomField(QueryTestCaseRequest request, List<TestCaseDTO> returnList) {
        Map<String, List<CustomField>> projectFieldMap = baseCustomFieldService.getWorkspaceSystemFields(CustomFieldScene.TEST_CASE.name(), request.getWorkspaceId())
                .stream().collect(Collectors.groupingBy(CustomField::getProjectId));

        Map<String, Map<String, String>> projectStatusOptionMap = new HashMap<>();
        Map<String, Map<String, String>> projectPriorityOptionMap = new HashMap<>();
        projectFieldMap.forEach((projectId, fields) -> {
            Map<String, String> statusOptionMap = Optional.ofNullable(projectStatusOptionMap.get(projectId)).orElse(new HashMap<>());
            Map<String, String> priorityOptionMap = Optional.ofNullable(projectPriorityOptionMap.get(projectId)).orElse(new HashMap<>());
            for (CustomField field : fields) {
                if (field.getName().equals(TestCaseImportFiled.STATUS.getFiledLangMap().get(Locale.SIMPLIFIED_CHINESE))) {
                    if (StringUtils.isNotBlank(field.getOptions())) {
                        statusOptionMap = JSON.parseArray(field.getOptions(), CustomFieldOptionDTO.class)
                                .stream()
                                .collect(Collectors.toMap(CustomFieldOptionDTO::getValue, CustomFieldOptionDTO::getText));
                    }
                }
                if (field.getName().equals(TestCaseImportFiled.PRIORITY.getFiledLangMap().get(Locale.SIMPLIFIED_CHINESE))) {
                    if (StringUtils.isNotBlank(field.getOptions())) {
                        priorityOptionMap = JSON.parseArray(field.getOptions(), CustomFieldOptionDTO.class)
                                .stream()
                                .collect(Collectors.toMap(CustomFieldOptionDTO::getValue, CustomFieldOptionDTO::getText));
                    }
                }
            }
            projectStatusOptionMap.put(projectId, statusOptionMap);
            projectPriorityOptionMap.put(projectId, priorityOptionMap);
        });

        returnList.forEach(testCase -> {
            Map<String, String> statusMap = projectStatusOptionMap.get(testCase.getProjectId());
            Map<String, String> priorityMap = projectPriorityOptionMap.get(testCase.getProjectId());
            String status = statusMap == null ? testCase.getStatus() : statusMap.get(testCase.getStatus());
            String priority = priorityMap == null ? testCase.getPriority() : priorityMap.get(testCase.getPriority());
            if (StringUtils.isNotBlank(status)) {
                testCase.setStatus(status);
            }
            if (StringUtils.isNotBlank(priority)) {
                testCase.setPriority(priority);
            }
        });
    }


    public void setDefaultOrder(QueryTestCaseRequest request) {
        List<OrderRequest> orders = ServiceUtils.getDefaultSortOrder(request.getOrders());
        orders.forEach(i -> i.setPrefix("test_case"));
        request.setOrders(orders);
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
        addVersionName(list);
        return list;
    }

    public void addProjectName(List<TestCaseDTO> list) {
        List<String> projectIds = list.stream()
                .map(TestCase::getProjectId)
                .collect(Collectors.toList());
        List<Project> projects = baseProjectService.getProjectByIds(projectIds);
        Map<String, String> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getId, Project::getName));
        list.forEach(item -> {
            String projectName = projectMap.get(item.getProjectId());
            if (StringUtils.isNotBlank(projectName)) {
                item.setProjectName(projectName);
            }
        });
    }

    public void addVersionName(List<TestCaseDTO> list) {
        List<String> versionIds = list.stream().map(TestCase::getVersionId).collect(Collectors.toList());
        List<ProjectVersion> versions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(versionIds)) {
            ProjectVersionExample example = new ProjectVersionExample();
            example.createCriteria().andIdIn(versionIds);
            versions = projectVersionMapper.selectByExample(example);
        }
        Map<String, String> projectVersionMap = versions.stream()
                .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
        list.forEach(item -> {
            String versionName = projectVersionMap.get(item.getVersionId());
            if (StringUtils.isNotBlank(versionName)) {
                item.setVersionName(versionName);
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
        if (StringUtils.equals(request.getImportType(), ExcelImportType.Create.name())) {
            // 创建如果没选版本就创建最新版本，更新时没选就更新最近版本的用例
            if (StringUtils.isBlank(request.getVersionId())) {
                request.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
            }
            int nextNum = getNextNum(request.getProjectId());
            importCreateNum.set(nextNum);
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
        Project project = baseProjectService.getProjectById(projectId);
        boolean useCunstomId = trackProjectService.useCustomNum(project);

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
                        this.saveImportData(xmindParser.getTestCase(), request, null);
                        names = xmindParser.getTestCase().stream().map(TestCase::getName).collect(Collectors.toList());
                        ids = xmindParser.getTestCase().stream().map(TestCase::getId).collect(Collectors.toList());
                    }
                    if (CollectionUtils.isNotEmpty(xmindParser.getUpdateTestCase())) {
                        this.updateImportData(xmindParser.getUpdateTestCase(), request, null);
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
                        this.updateImportData(xmindParser.getUpdateTestCase(), request, null);
                        names = xmindParser.getTestCase().stream().map(TestCase::getName).collect(Collectors.toList());
                        ids = xmindParser.getTestCase().stream().map(TestCase::getId).collect(Collectors.toList());
                    }
                    List<String> nodePathList = xmindParser.getValidatedNodePath();
                    if (CollectionUtils.isNotEmpty(nodePathList)) {
                        testCaseNodeService.createNodes(nodePathList, projectId);
                    }
                    if (CollectionUtils.isNotEmpty(continueCaseList)) {
                        this.saveImportData(continueCaseList, request, null);
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
        if (CollectionUtils.isNotEmpty(errList)) {
            int errorNum = 1;
            for (ExcelErrData<TestCaseExcelData> errData : errList) {
                errData.setRowNum(errorNum++);
            }
        }
        return getImportResponse(errList, true);
    }

    private ExcelResponse testCaseExcelImport(MultipartFile multipartFile, TestCaseImportRequest request,
                                              HttpServletRequest httpRequest) {
        String projectId = request.getProjectId();
        Set<String> userIds;
        Project project = baseProjectService.getProjectById(projectId);
        boolean useCustomId = trackProjectService.useCustomNum(project);

        Set<String> savedIds = new HashSet<>();
        Set<String> testCaseNames = new HashSet<>();
        List<ExcelErrData<TestCaseExcelData>> errList = new ArrayList<>();
        boolean isUpdated = false;

        List<TestCase> testCases = getTestCaseForImport(projectId);
        for (TestCase testCase : testCases) {
            if (useCustomId) {
                savedIds.add(testCase.getCustomNum());
            } else {
                savedIds.add(String.valueOf(testCase.getNum()));
            }

            testCaseNames.add(testCase.getName());
        }

        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setProjectId(projectId);
        userIds = baseUserService.getProjectMemberList(queryMemberRequest)
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        try {
            //根据本地语言环境选择用哪种数据对象进行存放读取的数据
            Class clazz = new TestCaseExcelDataFactory().getExcelDataByLocal();
            TrackTestCaseTemplateService trackTestCaseTemplateService = CommonBeanFactory.getBean(TrackTestCaseTemplateService.class);
            TestCaseTemplateDao testCaseTemplate = trackTestCaseTemplateService.getTemplate(projectId);
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
            request.setUseCustomId(useCustomId);
            Set<ExcelMergeInfo> mergeInfoSet = new TreeSet<>();

            // 预处理，查询合并单元格信息
            EasyExcel.read(multipartFile.getInputStream(), null, new TestCasePretreatmentListener(mergeInfoSet))
                    .extraRead(CellExtraTypeEnum.MERGE).sheet().doRead();

            TestCaseNoModelDataListener easyExcelListener = new TestCaseNoModelDataListener(request, clazz, mergeInfoSet);

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

    public void saveImportData(List<TestCaseWithBLOBs> testCases, TestCaseImportRequest request,
                               Map<String, List<CustomFieldResourceDTO>> testCaseCustomFieldMap) {
        String projectId = request.getProjectId();
        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        Project project = baseProjectService.getProjectById(projectId);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        CustomFieldTestCaseMapper customFieldTestCaseMapper = sqlSession.getMapper(CustomFieldTestCaseMapper.class);

        ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
        boolean customNum = config.getCaseCustomNum();
        try {
            Long nextOrder = ServiceUtils.getNextOrder(projectId, extTestCaseMapper::getLastOrder);

            if (!testCases.isEmpty()) {
                Integer num = Math.max(importCreateNum.get(), getNextNum(request.getProjectId()));
                for (int i = testCases.size() - 1; i > -1; i--) { // 反向遍历，保持和文件顺序一致
                    TestCaseWithBLOBs testCase = testCases.get(i);
                    if (StringUtils.isBlank(testCase.getId())) {
                        testCase.setId(UUID.randomUUID().toString());
                    }
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
                    if (StringUtils.isBlank(testCase.getStatus())) {
                        testCase.setStatus(TestCaseReviewStatus.Prepare.name());
                    }
                    testCase.setOrder(nextOrder);
                    testCase.setRefId(testCase.getId());
                    testCase.setVersionId(request.getVersionId());
                    testCase.setLatest(true);
                    mapper.insert(testCase);
                    nextOrder += ServiceUtils.ORDER_STEP;
                    batchInsertCustomFieldTestCase(testCaseCustomFieldMap, customFieldTestCaseMapper, testCase);
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
    public void updateImportData(List<TestCaseWithBLOBs> testCases, TestCaseImportRequest request, Map<String, List<CustomFieldResourceDTO>> testCaseCustomFieldMap) {

        String projectId = request.getProjectId();
        List<TestCase> insertCases = new ArrayList<>();
        Map<String, String> nodePathMap = testCaseNodeService.createNodeByTestCases(testCases, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        CustomFieldTestCaseMapper customFieldTestCaseMapper = sqlSession.getMapper(CustomFieldTestCaseMapper.class);

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
                testCases.forEach(testCase -> {
                    testCase.setUpdateTime(System.currentTimeMillis());
                    testCase.setNodeId(nodePathMap.get(testCase.getNodePath()));
                    TestCase dbCase = request.isUseCustomId() ? customIdMap.get(testCase.getCustomNum()) : customIdMap.get(testCase.getNum());
                    testCase.setId(dbCase.getId());
                    testCase.setRefId(dbCase.getRefId());
                    if (StringUtils.isBlank(request.getVersionId())) {
                        request.setVersionId(baseProjectVersionMapper.getDefaultVersion(projectId));
                    }
                    // 选了版本就更新到对应的版本
                    if (dbCase.getVersionId().equals(request.getVersionId())) {
                        mapper.updateByPrimaryKeySelective(testCase);

                        // 先删除
                        if (MapUtils.isNotEmpty(testCaseCustomFieldMap)) {
                            CustomFieldTestCaseExample customFieldTestCaseExample = new CustomFieldTestCaseExample();
                            customFieldTestCaseExample.createCriteria().andResourceIdEqualTo(testCase.getId());
                            customFieldTestCaseMapper.deleteByExample(customFieldTestCaseExample);
                        }

                        // 再添加
                        batchInsertCustomFieldTestCase(testCaseCustomFieldMap, customFieldTestCaseMapper, testCase);

                    } else { // 没有对应的版本就新建对应版本用例
                        testCase.setCreateTime(System.currentTimeMillis());
                        testCase.setVersionId(request.getVersionId());
                        testCase.setId(UUID.randomUUID().toString());
                        testCase.setOrder(dbCase.getOrder());
                        testCase.setCreateUser(SessionUtils.getUserId());
                        testCase.setCustomNum(dbCase.getCustomNum());
                        testCase.setNum(dbCase.getNum());
                        testCase.setLatest(false);
                        testCase.setType(dbCase.getType());
                        if (StringUtils.isBlank(testCase.getStatus())) {
                            testCase.setStatus(TestCaseReviewStatus.Prepare.name());
                        }
                        testCase.setReviewStatus(TestCaseReviewStatus.Prepare.name());
                        insertCases.add(testCase); // 由于是批处理，这里先保存，最后再执行
                        mapper.insert(testCase);

                        batchInsertCustomFieldTestCase(testCaseCustomFieldMap, customFieldTestCaseMapper, testCase);
                    }
                });
            }
            sqlSession.flushStatements();

            insertCases.forEach(item -> {
                checkAndSetLatestVersion(item.getRefId());
            });
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void batchInsertCustomFieldTestCase(Map<String, List<CustomFieldResourceDTO>> testCaseCustomFieldMap,
                                                CustomFieldTestCaseMapper customFieldTestCaseMapper, TestCaseWithBLOBs testCase) {
        if (MapUtils.isEmpty(testCaseCustomFieldMap)) {
            return;
        }
        List<CustomFieldResourceDTO> customFieldResources = testCaseCustomFieldMap.get(testCase.getId());
        if (CollectionUtils.isNotEmpty(customFieldResources)) {
            customFieldResources.forEach(customFieldTestCaseMapper::insert);
        }
    }

    public void download(String fileName, HttpServletResponse res) throws IOException {
        if (StringUtils.isEmpty(fileName)) {
            fileName = "xmind.xml";
        }
        // 发送给客户端的数据
        byte[] buff = new byte[1024];
        try (OutputStream outputStream = res.getOutputStream();
             BufferedInputStream bis = new BufferedInputStream(TestCaseService.class.getClassLoader().getResourceAsStream("xmind/" + fileName));) {
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
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            boolean isUseCustomId = trackProjectService.useCustomNum(projectId);
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("思维导图用例模版", StandardCharsets.UTF_8.name()) + ".xmind");
            String fileName = null;
            if (StringUtils.equals(importType, ExcelImportType.Update.name())) {
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

    private List<TestCaseDTO> generateExportData(String projectId) {
        List<TestCaseDTO> list = new ArrayList<>();
        StringBuilder path = new StringBuilder();
        for (int i = 1; i <= 4; i++) {
            path.append("/" + Translator.get("module") + i);
            TestCaseDTO testCaseDTO = new TestCaseDTO();
            testCaseDTO.setCustomNum(StringUtils.EMPTY);
            testCaseDTO.setName(Translator.get("test_case") + i);
            testCaseDTO.setNodePath(path.toString());
            testCaseDTO.setPriority("P" + i % 4);
            testCaseDTO.setRemark(Translator.get("remark_optional"));
            testCaseDTO.setPrerequisite(Translator.get("preconditions_optional"));
            List steps = new ArrayList();
            for (int j = 1; j <= 2; j++) {
                Map stepItem = new LinkedHashMap<>();
                stepItem.put("desc", Translator.get("test_case_step_desc") + j);
                stepItem.put("result", Translator.get("test_case_step_result") + j);
                steps.add(stepItem);
            }
            testCaseDTO.setSteps(JSON.toJSONString(steps));
            testCaseDTO.setProjectId(projectId);
            list.add(testCaseDTO);
        }
        return list;
    }

    public void testCaseExport(HttpServletResponse response, TestCaseExportRequest request) {
        String projectId = request.getProjectId();
        request.getCondition().setStatusIsNot(CommonConstants.TrashStatus);
        TestCaseBatchRequest batchRequest = new TestCaseBatchRequest();
        BeanUtils.copyBean(batchRequest, request);
        List<TestCaseDTO> testCases = getExportData(batchRequest);
        List<List<String>> headList = getTestcaseExportHeads(request);

        Map<Integer, Integer> rowMergeInfo = new HashMap<>();
        FunctionCaseMergeWriteHandler writeHandler = new FunctionCaseMergeWriteHandler(rowMergeInfo, headList);
        boolean isUseCustomId = trackProjectService.useCustomNum(projectId);

        Map<String, List<String>> caseLevelAndStatusValueMap = trackTestCaseTemplateService.getCaseLevelAndStatusMapByProjectId(projectId);
        FunctionCaseTemplateWriteHandler handler = new FunctionCaseTemplateWriteHandler(true, headList, caseLevelAndStatusValueMap);

        List<TestCaseExcelData> excelData = parseCaseData2ExcelData(testCases, rowMergeInfo, isUseCustomId, request.getOtherHeaders());
        List<List<Object>> data = parseExcelData2List(headList, excelData);
        new EasyExcelExporter(new TestCaseExcelDataFactory().getTestCaseExcelDataLocal().getClass())
                .exportByCustomWriteHandler(response, headList, data, Translator.get("test_case_import_template_name"),
                        Translator.get("test_case_import_template_sheet"), handler, writeHandler);
    }

    @NotNull
    private List<List<String>> getTestcaseExportHeads(TestCaseExportRequest request) {
        List<List<String>> headList = new ArrayList<>() {{
            addAll(request.getBaseHeaders()
                    .stream()
                    .map(item -> Arrays.asList(item.getName()))
                    .collect(Collectors.toList()));
            addAll(request.getCustomHeaders()
                    .stream()
                    .map(item -> Arrays.asList(item.getName()))
                    .collect(Collectors.toList()));
            addAll(request.getOtherHeaders()
                    .stream()
                    .map(item -> Arrays.asList(item.getName()))
                    .collect(Collectors.toList()));
        }};
        return headList;
    }

    public void testCaseTemplateExport(String projectId, String importType, HttpServletResponse response) {
        //导入更新 or 开启使用自定义ID时，导出ID列
        boolean needIdCol = trackProjectService.useCustomNum(projectId) || StringUtils.equals(importType, ExcelImportType.Update.name());

        List<List<String>> heads = getExportTemplateHeads(projectId, needIdCol);

        TestCaseExcelData testCaseExcelData = new TestCaseExcelDataFactory().getTestCaseExcelDataLocal();
        Map<Integer, Integer> rowMergeInfo = new HashMap<>();

        FunctionCaseMergeWriteHandler writeHandler = new FunctionCaseMergeWriteHandler(rowMergeInfo, heads);
        boolean isUseCustomId = trackProjectService.useCustomNum(projectId);

        Map<String, List<String>> caseLevelAndStatusValueMap = trackTestCaseTemplateService.getCaseLevelAndStatusMapByProjectId(projectId);
        FunctionCaseTemplateWriteHandler handler = new FunctionCaseTemplateWriteHandler(needIdCol, heads, caseLevelAndStatusValueMap);

        List<TestCaseExcelData> excelData = parseCaseData2ExcelData(generateExportData(projectId),
                rowMergeInfo, isUseCustomId, null);
        List<List<Object>> data = parseExcelData2List(heads, excelData);
        new EasyExcelExporter(testCaseExcelData.getClass())
                .exportByCustomWriteHandler(response, heads, data, Translator.get("test_case_import_template_name"),
                        Translator.get("test_case_import_template_sheet"), handler, writeHandler);
    }

    private List<List<String>> getExportTemplateHeads(String projectId, boolean needIdCol) {
        TestCaseTemplateDao testCaseTemplate = trackTestCaseTemplateService.getTemplate(projectId);
        List<CustomFieldDao> customFields = Optional.ofNullable(testCaseTemplate.getCustomFields()).orElse(new ArrayList<>());
        List<List<String>> heads = new TestCaseExcelDataFactory().getTestCaseExcelDataLocal().getHead(needIdCol, customFields);
        return heads;
    }


    public void testCaseXmindExport(HttpServletResponse response, TestCaseBatchRequest request) {
        try {
            request.getCondition().setStatusIsNot("Trash");
            if (request.getExportAll()) {
                // 导出所有用例, 勾选ID清空
                request.setIds(null);
            }
            List<TestCaseDTO> testCaseDTOList = this.findByBatchRequest(request);

            TestCaseXmindData rootXmindData = this.generateTestCaseXmind(testCaseDTOList);
            boolean isUseCustomId = trackProjectService.useCustomNum(request.getProjectId());
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

    private List<List<Object>> parseExcelData2List(List<List<String>> headListParams, List<TestCaseExcelData> data) {
        List<List<Object>> result = new ArrayList<>();
        //转化excel头
        List<String> headList = new ArrayList<>();
        for (List<String> list : headListParams) {
            for (String head : list) {
                headList.add(head);
            }
        }

        TestCaseImportFiled[] importFields = TestCaseImportFiled.values();


        for (TestCaseExcelData model : data) {
            List<Object> fields = new ArrayList<>();
            Map<String, Object> customDataMaps = Optional.ofNullable(model.getCustomData())
                    .orElse(new HashMap<>());
            Map<String, String> otherFieldMaps = Optional.ofNullable(model.getOtherFields())
                    .orElse(new HashMap<>());
            for (String head : headList) {
                boolean isSystemField = false;
                for (TestCaseImportFiled importFiled : importFields) {
                    if (importFiled.containsHead(head)) {
                        fields.add(importFiled.parseExcelDataValue(model));
                        isSystemField = true;
                    }
                }
                if (!isSystemField) {
                    Object value = customDataMaps.get(head);
                    if (value == null) {
                        value = otherFieldMaps.get(head);
                    }
                    if (value == null) {
                        value = StringUtils.EMPTY;
                    }
                    fields.add(value);
                }
            }
            result.add(fields);
        }

        return result;
    }

    public List<TestCaseDTO> findByBatchRequest(TestCaseBatchRequest request) {
        if (!request.getCondition().isSelectAll()) {
            request.getCondition().setIds(request.getIds());
        }
        return listTestCase(request.getCondition(), true);
    }

    public List<TestCaseDTO> getExportData(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        this.initRequest(request.getCondition(), true);
        setDefaultOrder(request.getCondition());
        Map<String, List<String>> filters = request.getCondition().getFilters();
        if (filters != null && !filters.containsKey("status")) {
            filters.put("status", new ArrayList<>(0));
        }
        List<TestCaseDTO> testCaseList = extTestCaseMapper.listByTestCaseIds(request);
        return testCaseList;
    }

    private List<TestCaseExcelData> parseCaseData2ExcelData(List<TestCaseDTO> testCaseList, Map<Integer, Integer> rowMergeInfo,
                                                            Boolean isUseCustomId, List<TestCaseExportRequest.TestCaseExportHeader> otherHeaders) {
        if (CollectionUtils.isEmpty(testCaseList)) {
            return new ArrayList<>();
        }
        List<TestCaseExcelData> list = new ArrayList<>();
        Map<String, Map<String, String>> customSelectValueMap = new HashMap<>();
        Map<String, String> customNameMap = new HashMap<>();
        String projectId = testCaseList.get(0).getProjectId();
        TrackTestCaseTemplateService trackTestCaseTemplateService = CommonBeanFactory.getBean(TrackTestCaseTemplateService.class);
        TestCaseTemplateDao testCaseTemplate = trackTestCaseTemplateService.getTemplate(projectId);

        List<CustomFieldDao> customFieldList;
        if (testCaseTemplate == null) {
            customFieldList = new ArrayList<>();
        } else {
            customFieldList = testCaseTemplate.getCustomFields();
        }

        Set<String> textFields = new HashSet<>();
        Map<String, String> userMap = baseUserService.getProjectMemberOption(projectId)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        buildExportCustomFieldMap(userMap, customSelectValueMap, customNameMap, customFieldList, textFields);

        for (int rowIndex = 0; rowIndex < testCaseList.size(); rowIndex++) {
            TestCaseDTO t = testCaseList.get(rowIndex);
            List<String> stepDescList = new ArrayList<>();
            List<String> stepResultList = new ArrayList<>();
            TestCaseExcelData data = new TestCaseExcelData();

            setExportSystemField(t, customNameMap, customSelectValueMap);
            BeanUtils.copyBean(data, t);
            data.setMaintainer(userMap.get(data.getMaintainer()));
            buildExportCustomNum(isUseCustomId, t, data);
            buildExportStep(t, stepDescList, stepResultList, data);
            buildExportCustomField(customSelectValueMap, customNameMap, t, data, textFields);
            buildExportOtherField(data, t, otherHeaders);

            if (CollectionUtils.isNotEmpty(stepDescList)) {
                // 如果有多条步骤则添加多条数据，之后合并单元格
                buildExportMergeData(rowMergeInfo, list, stepDescList, stepResultList, data);
            } else {
                list.add(data);
            }
        }
        return list;
    }

    private void buildExportOtherField(TestCaseExcelData data, TestCaseDTO t, List<TestCaseExportRequest.TestCaseExportHeader> otherHeaders) {
        if (CollectionUtils.isEmpty(otherHeaders)) {
            return;
        }
        List<String> keys = otherHeaders.stream()
                .map(TestCaseExportRequest.TestCaseExportHeader::getId)
                .collect(Collectors.toList());
        Map<String, TestCaseExportConverter> converterMaps = TestCaseExportConverterFactory.getConverters(keys);
        HashMap<String, String> otherFields = new HashMap<>();
        otherHeaders.forEach(header -> {
            TestCaseExportConverter converter = converterMaps.get(header.getId());
            if (converter != null) {
                otherFields.put(header.getName(), converter.parse(t));
            } else {
                otherFields.put(header.getName(), StringUtils.EMPTY);
            }
        });
        data.setOtherFields(otherFields);
    }

    private void buildExportCustomNum(Boolean isUseCustomId, TestCaseDTO t, TestCaseExcelData data) {
        if (isUseCustomId) {
            data.setCustomNum(t.getCustomNum());
        } else {
            if (t.getNum() == null) {
                data.setCustomNum(StringUtils.EMPTY);
            } else {
                data.setCustomNum(String.valueOf(t.getNum()));
            }
        }
    }

    @NotNull
    private void buildExportMergeData(Map<Integer, Integer> rowMergeInfo,
                                      List<TestCaseExcelData> list, List<String> stepDescList,
                                      List<String> stepResultList, TestCaseExcelData data) {
        for (int i = 0; i < stepDescList.size(); i++) {
            TestCaseExcelData excelData;
            if (i == 0) {
                // 第一行存全量元素
                excelData = data;
                if (stepDescList.size() > 1) {
                    // 保存合并单元格的下标和数量
                    rowMergeInfo.put(list.size() + 1, stepDescList.size());
                }
            } else {
                // 之后的行只存步骤
                excelData = new TestCaseExcelData();
            }
            excelData.setStepDesc(stepDescList.get(i));
            excelData.setStepResult(stepResultList.get(i));
            list.add(excelData);
        }
    }

    private void buildExportCustomField(Map<String, Map<String, String>> customSelectValueMap,
                                        Map<String, String> customNameMap, TestCaseDTO t, TestCaseExcelData data, Set<String> textFields) {
        try {
            List<CustomFieldResourceDTO> fields = customFieldTestCaseService.getByResourceId(t.getId());
            Map<String, Object> map = new HashMap<>();
            for (int index = 0; index < fields.size(); index++) {
                CustomFieldResourceDTO field = fields.get(index);
                //进行key value对换
                String id = field.getFieldId();
                if (textFields.contains(id)) {
                    map.put(customNameMap.get(id), field.getTextValue());
                    continue;
                }
                if (StringUtils.isNotBlank(field.getValue())) {
                    Object value = JSON.parseObject(field.getValue());
                    Map<String, String> optionMap = customSelectValueMap.get(id);
                    if (value instanceof List) {
                        List<String> results = new ArrayList<>();
                        List values = (List) value;
                        values.forEach(item -> {
                            if (MapUtils.isEmpty(optionMap)) {
                                results.add(item.toString());
                            } else if (optionMap.containsKey(item.toString())) {
                                results.add(optionMap.get(item.toString()));
                            }
                        });
                        map.put(customNameMap.get(id), results.toString());
                    } else {
                        if (MapUtils.isNotEmpty(optionMap) && optionMap.containsKey(value)) {
                            value = optionMap.get(value);
                        }
                        map.put(customNameMap.get(id), value.toString());
                    }
                }
            }
            data.setCustomData(map);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void buildExportStep(TestCaseDTO t, List<String> stepDescList, List<String> stepResultList, TestCaseExcelData data) {
        if (StringUtils.isBlank(t.getStepModel())) {
            data.setStepModel(TestCaseConstants.StepModel.STEP.name());
        } else {
            data.setStepModel(t.getStepModel());
        }
        if (StringUtils.equals(data.getStepModel(), TestCaseConstants.StepModel.TEXT.name())) {
            data.setStepDesc(t.getStepDescription());
            data.setStepResult(t.getExpectedResult());
        } else {
            String steps = t.getSteps();
            List jsonArray = new ArrayList();
            //解决旧版本保存用例导出报错
            try {
                jsonArray = JSON.parseArray(steps);
            } catch (Exception e) {
                if (steps.contains("null") && !steps.contains("\"null\"")) {
                    steps = steps.replace("null", "\"\"");
                    jsonArray = JSON.parseArray(steps);
                }
            }
            for (int j = 0; j < jsonArray.size(); j++) {
                // 将步骤存储起来，之后生成多条数据，再合并单元格
                Map item = (Map) jsonArray.get(j);
                String stepDesc = Optional.ofNullable(item.get("desc")).orElse(StringUtils.EMPTY).toString();
                String stepResult = Optional.ofNullable(item.get("result")).orElse(StringUtils.EMPTY).toString();
                if (StringUtils.isNotBlank(stepDesc) || StringUtils.isNotBlank(stepResult)) {
                    stepDescList.add(stepDesc);
                    stepResultList.add(stepResult);
                }
            }
        }
    }

    private void buildExportCustomFieldMap(Map<String, String> userMap, Map<String, Map<String, String>> customSelectValueMap,
                                           Map<String, String> customNameMap, List<CustomFieldDao> customFieldList, Set<String> textFields) {
        for (CustomFieldDao dto : customFieldList) {
            Map<String, String> map = new HashMap<>();
            if (CustomFieldType.getHasOptionValueSet().contains(dto.getType())) {
                try {
                    List<CustomFieldOptionDTO> options = JSON.parseArray(dto.getOptions(), CustomFieldOptionDTO.class);
                    options.forEach(option -> {
                        String text = option.getText();
                        String value = option.getValue();
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
                    });

                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
            if (StringUtils.equalsAny(dto.getType(), CustomFieldType.TEXTAREA.getValue(), CustomFieldType.RICH_TEXT.getValue())) {
                textFields.add(dto.getId());
            }
            if (StringUtils.equalsAny(dto.getType(), CustomFieldType.MULTIPLE_MEMBER.getValue(), CustomFieldType.MEMBER.getValue())) {
                customSelectValueMap.put(dto.getId(), userMap);
            } else {
                customSelectValueMap.put(dto.getId(), map);
            }
            customNameMap.put(dto.getId(), dto.getName());
        }
    }

    public void setExportSystemField(TestCaseDTO testCase, Map<String, String> customNameMap,
                                     Map<String, Map<String, String>> customSelectValueMap) {
        String statusKey = null;
        for (String k : customNameMap.keySet()) {
            String v = customNameMap.get(k);
            if (StringUtils.equals(v, "用例状态")) {
                statusKey = k;
            }
        }
        if (StringUtils.isNotEmpty(statusKey)) {
            Map<String, String> valueMap = customSelectValueMap.get(statusKey);
            testCase.setStatus(valueMap.get(testCase.getStatus()));
        }
    }

    /**
     * 更新自定义字段
     *
     * @param request
     */
    public void editTestCaseBath(TestCaseBatchRequest request) {
        if (request.getCustomField() != null) {
            batchEditField(request);
        } else if (StringUtils.equals("tags", request.getType())) {
            batchEditTag(request);
        } else {
            // 批量移动
            if (request.getCondition().isSelectAll()) {
                // 全选则重新设置MoveIds
                List<TestCaseDTO> testCaseDTOS = listTestCase(request.getCondition());
                List<String> ids = testCaseDTOS.stream().map(TestCaseDTO::getId).collect(Collectors.toList());
                request.setIds(ids);
            }
            TestCaseWithBLOBs batchEdit = new TestCaseWithBLOBs();
            BeanUtils.copyBean(batchEdit, request);
            batchEdit.setUpdateTime(System.currentTimeMillis());
            bathUpdateByCondition(request, batchEdit);
            //批量修改选中数据其他版本的模块路径
            if (request != null && (request.getIds() != null || !request.getIds().isEmpty())) {
                request.getIds().forEach(testCaseId -> {
                    TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(testCaseId);
                    if (testCaseWithBLOBs == null) {
                        return;
                    }
                    if (StringUtils.isNotEmpty(request.getNodeId()) && StringUtils.isNotEmpty(request.getNodePath())) {
                        testCaseWithBLOBs.setNodeId(request.getNodeId());
                        testCaseWithBLOBs.setNodePath(request.getNodePath());
                        EditTestCaseRequest editTestCaseRequest = new EditTestCaseRequest();
                        BeanUtils.copyBean(editTestCaseRequest, testCaseWithBLOBs);
                        updateOtherVersionModule(editTestCaseRequest);
                    }
                });
            }
        }
    }

    private void batchEditTag(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds()) || request.getTagList().isEmpty()) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        List<TestCase> testCaseList = testCaseMapper.selectByExample(example);
        for (TestCase tc : testCaseList) {
            String tags = tc.getTags();
            if (StringUtils.isBlank(tags) || BooleanUtils.isFalse(request.isAppendTag())) {
                tc.setTags(JSON.toJSONString(request.getTagList()));
            } else {
                try {
                    List<String> list = JSON.parseArray(tags, String.class);
                    list.addAll(request.getTagList());
                    tc.setTags(JSON.toJSONString(list));
                } catch (Exception e) {
                    LogUtil.error("batch edit tags error.");
                    LogUtil.error(e, e.getMessage());
                    tc.setTags(JSON.toJSONString(request.getTagList()));
                }
            }
            tc.setUpdateTime(System.currentTimeMillis());
            mapper.updateByPrimaryKey(tc);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void batchEditField(TestCaseBatchRequest request) {
        CustomFieldResourceDTO customField = request.getCustomField();
        String name = customField.getName();
        TestCaseWithBLOBs testCaseWithBLOBs = new TestCaseWithBLOBs();
        testCaseWithBLOBs.setUpdateTime(System.currentTimeMillis());

        if (StringUtils.equalsAnyIgnoreCase(name, "用例等级")) {
            testCaseWithBLOBs.setPriority(JSON.parseObject(customField.getValue(), String.class));
            bathUpdateByCondition(request, testCaseWithBLOBs);
        } else if (StringUtils.equals(name, "用例状态")) {
            testCaseWithBLOBs.setStatus(JSON.parseObject(customField.getValue(), String.class));
            bathUpdateByCondition(request, testCaseWithBLOBs);
        } else if (StringUtils.equals(name, "责任人")) {
            testCaseWithBLOBs.setMaintainer(JSON.parseObject(customField.getValue(), String.class));
            bathUpdateByCondition(request, testCaseWithBLOBs);
        } else {
            ServiceUtils.getSelectAllIds(request, request.getCondition(),
                    (query) -> extTestCaseMapper.selectIds(query));
            if (CollectionUtils.isEmpty(request.getIds())) {
                return;
            }

            customFieldTestCaseService.batchUpdateByResourceIds(request.getIds(), customField);
            // 如果没有字段，则添加
            customFieldTestCaseService.batchInsertIfNotExists(request.getIds(), customField);

            if (request.getCondition().isSelectAll()) {
                // 如果全选，去掉这个查询条件，避免ids过长
                request.getCondition().setIds(null);
            }
            // 更新修改时间
            bathUpdateByCondition(request, testCaseWithBLOBs);
        }

    }

    private int bathUpdateByCondition(TestCaseBatchRequest request, TestCaseWithBLOBs testCaseWithBLOBs) {
        return extTestCaseMapper.bathUpdateByCondition(request.getCondition(), testCaseWithBLOBs);
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
                batchCopy.setName("copy_" + batchCopy.getName());
                if (batchCopy.getName().length() > 255) {
                    batchCopy.setName(batchCopy.getName().substring(0, 250) + batchCopy.getName().substring(batchCopy.getName().length() - 5));
                }
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
                    String versionId = baseProjectVersionMapper.getDefaultVersion(SessionUtils.getCurrentProjectId());
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
        customFieldTestCaseService.deleteByResourceIds(request.getIds()); // 删除自定义字段
        //删除执行信息
        functionCaseExecutionInfoService.deleteBySourceIdList(request.getIds());

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
        List<TestCase> testCaseList = testCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testCaseList)) {
            List<String> idList = testCaseList.stream().map(TestCase::getId).collect(Collectors.toList());
            //删除执行记录
            functionCaseExecutionInfoService.deleteBySourceIdList(idList);
        }
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
            MSException.throwException(Translator.get("related_case_del_fail_prefix") + StringUtils.SPACE + str + StringUtils.SPACE + Translator.get("related_case_del_fail_suffix"));
        }
    }

    /**
     * 获取项目下一个num (页面展示的ID)
     *
     * @return
     */
    public int getNextNum(String projectId) {
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

    public TestCase add(EditTestCaseRequest request, List<MultipartFile> files) {
        final TestCaseWithBLOBs testCaseWithBLOBs = addTestCase(request);
        if (StringUtils.isNotEmpty(request.getCopyCaseId())) {
            // 复制用例时复制对应附件数据
            AttachmentRequest attachmentRequest = new AttachmentRequest();
            attachmentRequest.setCopyBelongId(request.getCopyCaseId());
            attachmentRequest.setBelongId(testCaseWithBLOBs.getId());
            attachmentRequest.setBelongType(AttachmentType.TEST_CASE.type());
            attachmentService.copyAttachment(attachmentRequest);
        } else {
            // 新增需上传用例所有待上传的附件
            if (CollectionUtils.isNotEmpty(files)) {
                files.forEach(file -> {
                    AttachmentRequest attachmentRequest = new AttachmentRequest();
                    attachmentRequest.setBelongId(testCaseWithBLOBs.getId());
                    attachmentRequest.setBelongType(AttachmentType.TEST_CASE.type());
                    attachmentService.uploadAttachment(attachmentRequest, file);
                });
            }
            // 同步待关联的文件附件, 生成关联记录
            if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                FileAssociationMapper associationBatchMapper = sqlSession.getMapper(FileAssociationMapper.class);
                AttachmentModuleRelationMapper attachmentModuleRelationBatchMapper = sqlSession.getMapper(AttachmentModuleRelationMapper.class);
                FileAttachmentMetadataMapper fileAttachmentMetadataBatchMapper = sqlSession.getMapper(FileAttachmentMetadataMapper.class);
                request.getRelateFileMetaIds().forEach(filemetaId -> {
                    FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(filemetaId);
                    FileAssociation fileAssociation = new FileAssociation();
                    fileAssociation.setId(UUID.randomUUID().toString());
                    fileAssociation.setFileMetadataId(filemetaId);
                    fileAssociation.setFileType(fileMetadata.getType());
                    fileAssociation.setType(FileAssociationType.TEST_CASE.name());
                    fileAssociation.setProjectId(fileMetadata.getProjectId());
                    fileAssociation.setSourceItemId(filemetaId);
                    fileAssociation.setSourceId(testCaseWithBLOBs.getId());
                    associationBatchMapper.insert(fileAssociation);
                    AttachmentModuleRelation record = new AttachmentModuleRelation();
                    record.setRelationId(testCaseWithBLOBs.getId());
                    record.setRelationType(AttachmentType.TEST_CASE.type());
                    record.setFileMetadataRefId(fileAssociation.getId());
                    record.setAttachmentId(UUID.randomUUID().toString());
                    attachmentModuleRelationBatchMapper.insert(record);
                    FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
                    BeanUtils.copyBean(fileAttachmentMetadata, fileMetadata);
                    fileAttachmentMetadata.setId(record.getAttachmentId());
                    fileAttachmentMetadata.setCreator(SessionUtils.getUserId());
                    fileAttachmentMetadata.setFilePath(fileMetadata.getPath() == null ? StringUtils.EMPTY : fileMetadata.getPath());
                    fileAttachmentMetadataBatchMapper.insert(fileAttachmentMetadata);
                });
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
        return testCaseWithBLOBs;
    }

    public TestCase edit(EditTestCaseRequest request) {
        TestCaseWithBLOBs testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(request.getId());
        request.setNum(testCaseWithBLOBs.getNum());
        if (testCaseWithBLOBs == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + request.getId());
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
        List<FileMetadata> originFiles = attachmentService.getFileMetadataByCaseId(testCaseId);
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

        deleteToGcBatch(request.getIds(), request.getProjectId());

        testCaseNodeService.minderEdit(request);

        List<TestCaseMinderEditRequest.TestCaseMinderEditItem> data = request.getData();
        if (CollectionUtils.isNotEmpty(data)) {

            String lastAddId = null;

            for (TestCaseMinderEditRequest.TestCaseMinderEditItem item : data) {
                if (StringUtils.isBlank(item.getNodeId()) || item.getNodeId().equals("root")) {
                    item.setNodeId(StringUtils.EMPTY);
                }
                item.setProjectId(request.getProjectId());
                if (item.getIsEdit()) {
                    EditTestCaseRequest editRequest = new EditTestCaseRequest();
                    BeanUtils.copyBean(editRequest, item);
                    editRequest.setCustomFields(null);
                    editRequest.setTags(null);
                    editTestCase(editRequest);
                    changeOrder(item, request.getProjectId());
                    lastAddId = null;
                } else {
                    if (StringUtils.isBlank(item.getMaintainer())) {
                        item.setMaintainer(SessionUtils.getUserId());
                    }
                    EditTestCaseRequest editTestCaseRequest = new EditTestCaseRequest();
                    BeanUtils.copyBean(editTestCaseRequest, item);
                    addTestCase(editTestCaseRequest);
                    if (StringUtils.equals(item.getMoveMode(), ResetOrderRequest.MoveMode.APPEND.name()) && StringUtils.isNotBlank(lastAddId)) {
                        item.setMoveMode(ResetOrderRequest.MoveMode.AFTER.name());
                        item.setTargetId(lastAddId);
                    }
                    lastAddId = editTestCaseRequest.getId();
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

    public List<TestCase> getTestCaseByProjectId(String projectId) {
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash").andLatestEqualTo(true);
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
                List<IssuesDao> list = issueMap.get(item.getResourceId());
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(item);
                issueMap.put(item.getResourceId(), list);
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
                    List<ApiTestCase> testCases = relevanceApiCaseService.getApiCaseByIds(testCaseIds);
                    List<String> caseNames = testCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(caseNames)) {
                        nameBuilder.append("接口用例：").append(StringUtils.LF).append(caseNames).append(StringUtils.LF);
                    }
                }
                if (CollectionUtils.isNotEmpty(performanceIds)) {
                    List<LoadTest> loadTests = relevanceLoadCaseService.getLoadCaseByIds(performanceIds);
                    List<String> caseNames = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(caseNames)) {
                        nameBuilder.append("性能用例：").append(StringUtils.LF).append(caseNames).append(StringUtils.LF);
                    }
                }
                if (CollectionUtils.isNotEmpty(automationIds)) {
                    List<ApiScenario> scenarios = relevanceApiCaseService.getScenarioCaseByIds(automationIds);
                    List<String> caseNames = scenarios.stream().map(ApiScenario::getName).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(caseNames)) {
                        nameBuilder.append("自动化用例：").append(StringUtils.LF).append(caseNames).append(StringUtils.LF);
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
            List<FileMetadata> originFiles = attachmentService.getFileMetadataByCaseId(id);
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
            DetailColumn detailColumn = new DetailColumn("评论", "comment", String.join(StringUtils.LF, names), null);
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
                DetailColumn detailColumn = new DetailColumn("评论", "comment", String.join(StringUtils.LF, names), null);
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
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extTestCaseMapper.selectIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            extTestCaseMapper.checkOriginalStatusByIds(ids);

            //检查原来模块是否还在
            TestCaseExample example = new TestCaseExample();
            // 关联版本之后，必须查询每一个数据的所有版本，依次还原
            example.createCriteria().andIdIn(ids);
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
            testPlanTestCaseService.reduction(request.getIds());
            testReviewTestCaseService.reduction(request.getIds());
        }
    }

    public void deleteToGcBatchPublic(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectPublicIds(query));
        List<String> ids = request.getIds();
        List<String> needDelete = new ArrayList<>();
        List<String> noDelete = new ArrayList<>();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        if (CollectionUtils.isNotEmpty(ids)) {
            for (String id : ids) {
                TestCase testCase = testCaseMapper.selectByPrimaryKey(id);
                if ((StringUtils.isNotEmpty(testCase.getMaintainer()) && testCase.getMaintainer().equals(SessionUtils.getUserId())) ||
                        (StringUtils.isNotEmpty(testCase.getCreateUser()) && testCase.getCreateUser().equals(SessionUtils.getUserId()))) {
                    needDelete.add(testCase.getRefId());
                } else {
                    noDelete.add(testCase.getName());
                }
            }
        }
        try {
            if (CollectionUtils.isNotEmpty(needDelete)) {
                for (int i = 0; i < needDelete.size(); i++) {
                    TestCaseExample e = new TestCaseExample();
                    e.createCriteria().andRefIdEqualTo(needDelete.get(i));
                    TestCaseWithBLOBs t = new TestCaseWithBLOBs();
                    t.setCasePublic(false);
                    mapper.updateByExampleSelective(t, e);
                    if (i % 50 == 0)
                        sqlSession.flushStatements();
                }
                sqlSession.flushStatements();
            }
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        if (CollectionUtils.isNotEmpty(noDelete)) {
            MSException.throwException(Translator.get("check_owner_case"));
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

    //    public List<ApiTestCaseDTO> getTestCaseApiCaseRelateList(ApiTestCaseRequest request) {
    //        List<ApiTestCaseDTO> apiTestCaseDTOS = testCaseTestMapper.relevanceApiList(request);
    //        ServiceUtils.buildVersionInfo(apiTestCaseDTOS);
    //        return apiTestCaseDTOS;
    //    }
    //
    //    public List<ApiScenarioDTO> getTestCaseScenarioCaseRelateList(ApiScenarioRequest request) {
    //        List<ApiScenarioDTO> apiScenarioDTOS = testCaseTestMapper.relevanceScenarioList(request);
    //        ServiceUtils.buildVersionInfo(apiScenarioDTOS);
    //        return apiScenarioDTOS;
    //    }
    //
    //    public List<LoadTestDTO> getTestCaseLoadCaseRelateList(LoadCaseRequest request) {
    //        List<LoadTestDTO> loadTestDTOS = testCaseTestMapper.relevanceLoadList(request);
    //        ServiceUtils.buildVersionInfo(loadTestDTOS);
    //        return loadTestDTOS;
    //    }

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

        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();

        List<String> projectIds = new ArrayList<>();
        List<String> versionIds = new ArrayList<>();
        List<ApiTestCase> apiCases = new ArrayList<>();
        List<ApiScenario> apiScenarios = new ArrayList<>();
        List<LoadTest> apiLoadTests = new ArrayList<>();
        List<UiScenario> uiScenarios = new ArrayList<>();

        if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
            apiCases = relevanceApiCaseService.getApiCaseByIds(
                    getTestIds(testCaseTests, TestCaseTestType.testcase.name()));
            apiScenarios = relevanceApiCaseService.getScenarioCaseByIds(
                    getTestIds(testCaseTests, TestCaseTestType.automation.name()));
            projectIds.addAll(apiCases.stream().map(s -> s.getProjectId()).collect(Collectors.toList()));
            projectIds.addAll(apiScenarios.stream().map(s -> s.getProjectId()).collect(Collectors.toList()));
            versionIds.addAll(apiCases.stream().map(s -> s.getVersionId()).collect(Collectors.toList()));
            versionIds.addAll(apiScenarios.stream().map(s -> s.getVersionId()).collect(Collectors.toList()));
        }

        if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
            apiLoadTests = relevanceLoadCaseService.getLoadCaseByIds(
                    getTestIds(testCaseTests, TestCaseTestType.performance.name()));
            projectIds.addAll(apiLoadTests.stream().map(s -> s.getProjectId()).collect(Collectors.toList()));
            versionIds.addAll(apiLoadTests.stream().map(l -> l.getVersionId()).collect(Collectors.toList()));
        }

        if (serviceIdSet.contains(MicroServiceName.UI_TEST)) {
            uiScenarios = relevanceUiCaseService.getUiCaseByIds(
                    getTestIds(testCaseTests, TestCaseTestType.uiAutomation.name()));
            projectIds.addAll(uiScenarios.stream().map(s -> s.getProjectId()).collect(Collectors.toList()));
            versionIds.addAll(uiScenarios.stream().map(l -> l.getVersionId()).collect(Collectors.toList()));
        }


        projectIds = projectIds.stream().distinct().collect(Collectors.toList());
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

        Map<String, String> versionNameMap = projectVersions.stream().collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));

        List<TestCaseTestDao> testCaseTestList = new ArrayList<>();
        apiCases.forEach(item -> {
            getTestCaseTestDaoList(TestCaseTestType.testcase.name(), item.getNum(), item.getName(), item.getId(), projectNameMap.get(item.getProjectId()), versionNameMap.get(item.getVersionId()),
                    testCaseTestList, testCaseTestsMap);
        });
        apiScenarios.forEach(item -> {
            getTestCaseTestDaoList(TestCaseTestType.automation.name(), item.getNum(), item.getName(), item.getId(), projectNameMap.get(item.getProjectId()), versionNameMap.get(item.getVersionId()),
                    testCaseTestList, testCaseTestsMap);
        });
        apiLoadTests.forEach(item -> {
            getTestCaseTestDaoList(TestCaseTestType.performance.name(), item.getNum(), item.getName(), item.getId(), projectNameMap.get(item.getProjectId()), versionNameMap.get(item.getVersionId()),
                    testCaseTestList, testCaseTestsMap);
        });
        uiScenarios.forEach(item -> {
            getTestCaseTestDaoList(TestCaseTestType.uiAutomation.name(), item.getNum(), item.getName(), item.getId(), projectNameMap.get(item.getProjectId()), versionNameMap.get(item.getVersionId()),
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

    public void initAttachment() {
        // 用例有关附件处理
        AttachmentModuleRelationExample relationExample = new AttachmentModuleRelationExample();
        relationExample.createCriteria().andRelationTypeEqualTo(AttachmentType.TEST_CASE.type());
        List<AttachmentModuleRelation> relations = attachmentModuleRelationMapper.selectByExample(relationExample);
        Map<String, List<AttachmentModuleRelation>> relationGroup = relations.stream().collect(Collectors.groupingBy(AttachmentModuleRelation::getRelationId));
        for (Map.Entry<String, List<AttachmentModuleRelation>> entry : relationGroup.entrySet()) {
            final String caseId = entry.getKey();
            final String uploadPath = FileUtils.ATTACHMENT_DIR + File.separator + AttachmentType.TEST_CASE.type() + File.separator + caseId;
            // 获取同一用例关联的文件ID
            List<String> fileIds = entry.getValue().stream().map(AttachmentModuleRelation::getAttachmentId).collect(Collectors.toList());
            // 只在每次循环时查询目标用例下附件数据, 防止附件数据过大OOM
            FileMetadataExample fileMetadataExample = new FileMetadataExample();
            fileMetadataExample.createCriteria().andIdIn(fileIds);
            List<FileMetadata> allCaseFileMetadatas = fileMetadataMapper.selectByExample(fileMetadataExample);
            FileContentExample fileContentExample = new FileContentExample();
            fileContentExample.createCriteria().andFileIdIn(fileIds);
            List<FileContent> allCaseFileContents = fileContentMapper.selectByExample(fileContentExample);
            entry.getValue().stream().forEach(relation -> {
                String filename = StringUtils.EMPTY;
                List<FileMetadata> fileMetadatas = allCaseFileMetadatas.stream().filter(fileMetadata -> fileMetadata.getId().equals(relation.getAttachmentId()))
                        .collect(Collectors.toList());
                List<FileContent> fileContents = allCaseFileContents.stream().filter(fileContent -> fileContent.getFileId().equals(relation.getAttachmentId()))
                        .collect(Collectors.toList());
                if (fileMetadatas.size() == 1) {
                    FileMetadata fileMetadata = fileMetadatas.get(0);
                    filename = fileMetadata.getName();
                    FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
                    BeanUtils.copyBean(fileAttachmentMetadata, fileMetadata);
                    fileAttachmentMetadata.setId(UUID.randomUUID().toString());
                    fileAttachmentMetadata.setCreator(StringUtils.EMPTY);
                    fileAttachmentMetadata.setFilePath(uploadPath);
                    fileAttachmentMetadataMapper.insert(fileAttachmentMetadata);
                    AttachmentModuleRelation record = new AttachmentModuleRelation();
                    record.setAttachmentId(fileAttachmentMetadata.getId());
                    AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
                    example.createCriteria().andRelationIdEqualTo(relation.getRelationId())
                            .andAttachmentIdEqualTo(relation.getAttachmentId()).andRelationTypeEqualTo(relation.getRelationType());
                    attachmentModuleRelationMapper.updateByExample(record, example);
                    fileMetadataMapper.deleteByPrimaryKey(fileMetadata.getId());
                }
                if (StringUtils.isNotEmpty(filename) && fileContents.size() == 1) {
                    byte[] bytes = fileContents.get(0).getFile();
                    FileUtils.byteToFile(bytes, uploadPath, filename);
                    fileContentMapper.deleteByPrimaryKey(fileContents.get(0).getFileId());
                }
            });
        }
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

    public void updateLastExecuteStatus(List<String> ids, String status) {
        if (CollectionUtils.isNotEmpty(ids) && StringUtils.isNotBlank(status)) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(ids);
            TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
            testCase.setLastExecuteResult(status);
            testCaseMapper.updateByExampleSelective(testCase, example);
        }
    }

    public void updateLastExecuteStatus(String id, String status) {
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(status)) {
            this.updateLastExecuteStatus(Arrays.asList(id), status);
        }
    }

    public void updateReviewStatus(List<String> ids, String status) {
        if (CollectionUtils.isNotEmpty(ids) && StringUtils.isNotBlank(status)) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(ids);
            TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
            testCase.setReviewStatus(status);
            testCaseMapper.updateByExampleSelective(testCase, example);
        }
    }

    public void updateReviewStatus(String id, String status) {
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(status)) {
            this.updateReviewStatus(Arrays.asList(id), status);
        }
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

            Map<String, String> versionNameMap = new HashMap<>();
            List<String> versionIds = testCaseList.stream().map(TestCase::getVersionId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(versionIds)) {
                ProjectVersionRequest pvr = new ProjectVersionRequest();
                pvr.setProjectId(testCaseList.get(0).getProjectId());
                List<ProjectVersionDTO> projectVersions = baseProjectVersionMapper.selectProjectVersionList(pvr);
                versionNameMap = projectVersions.stream().collect(Collectors.toMap(ProjectVersionDTO::getId, ProjectVersionDTO::getName));
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
                relationshipEdgeDTO.setVersionName(versionNameMap.get(testCase.getVersionId()));
                results.add(relationshipEdgeDTO);
            }
            return results;
        }
        return new ArrayList<>();
    }

    public void buildProjectInfo(String projectId, List<TestCaseDTO> list) {
        Project project = baseProjectService.getProjectById(projectId);
        list.forEach(item -> {
            item.setProjectName(project.getName());
        });
    }

    public void buildUserInfo(List<TestCaseDTO> testCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(testCases.stream().map(TestCase::getCreateUser).collect(Collectors.toList()));
        userIds.addAll(testCases.stream().map(TestCase::getDeleteUserId).collect(Collectors.toList()));
        userIds.addAll(testCases.stream().map(TestCase::getMaintainer).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            testCases.forEach(caseResult -> {
                caseResult.setCreateName(userMap.get(caseResult.getCreateUser()));
                caseResult.setDeleteUserId(userMap.get(caseResult.getDeleteUserId()));
                caseResult.setMaintainerName(userMap.get(caseResult.getMaintainer()));
            });
        }
    }

    public void buildVersionInfo(List<TestCaseDTO> testCases) {
        List<String> versionIds = testCases.stream().map(TestCaseDTO::getVersionId).collect(Collectors.toList());
        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
        if (projectVersionService == null) {
            return;
        }
        Map<String, String> projectVersionMap = projectVersionService.getProjectVersionByIds(versionIds).stream()
                .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
        testCases.forEach(testCase -> {
            testCase.setVersionName(projectVersionMap.get(testCase.getVersionId()));
        });
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
                if (testCase.getName().length() > 255) {
                    testCase.setName(testCase.getName().substring(0, 250) + testCase.getName().substring(testCase.getName().length() - 5));
                }
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
        if (CommonConstants.TrashStatus.equalsIgnoreCase(testCase.getStatus())) {
            request.setFilters(new HashMap<>() {{
                put("status", new ArrayList() {{
                    add(CommonConstants.TrashStatus);
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
            List<String> idList = testCaseList.stream().map(TestCase::getId).collect(Collectors.toList());
            //删除执行记录
            functionCaseExecutionInfoService.deleteBySourceIdList(idList);
            //检查最新版本
            checkAndSetLatestVersion(refId);
        }
    }


    /**
     * 检查设置最新版本
     */
    private void checkAndSetLatestVersion(String refId) {
        extTestCaseMapper.clearLatestVersion(refId);
        extTestCaseMapper.addLatestVersion(refId);
    }

    public void deleteTestCasePublic(String versionId, String refId) {
        TestCase testCase = new TestCase();
        testCase.setRefId(refId);
        testCase.setVersionId(versionId);
        extTestCaseMapper.deletePublic(testCase);
    }

    public Boolean hasOtherInfo(String caseId) {
        TestCaseWithBLOBs tc = getTestCase(caseId);
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(caseId).andRelationTypeEqualTo(AttachmentType.TEST_CASE.type());
        if (tc != null) {
            if (StringUtils.isNotBlank(tc.getRemark()) || StringUtils.isNotBlank(tc.getDemandId()) || CollectionUtils.isNotEmpty(getRelateTest(caseId))
                    || CollectionUtils.isNotEmpty(issuesService.getIssues(caseId, IssueRefType.FUNCTIONAL.name())) || CollectionUtils.isNotEmpty(getRelationshipCase(caseId, "PRE")) || CollectionUtils.isNotEmpty(getRelationshipCase(caseId, "POST"))
                    || CollectionUtils.isNotEmpty(attachmentModuleRelationMapper.selectByExample(example))) {
                return true;
            }
        }
        return false;
    }

    public void saveRelationshipBatch(TestCaseRelationshipEdgeRequest request) {
        List<String> relationshipIds = relationshipEdgeService.getRelationshipIds(request.getId());
        request.getCondition().setNotInIds(relationshipIds);
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        List<String> ids = request.getIds();
        ids.remove(request.getId());
        if (CollectionUtils.isNotEmpty(ids)) {
            if (CollectionUtils.isNotEmpty(request.getTargetIds())) {
                request.setTargetIds(ids);
            } else if (CollectionUtils.isNotEmpty(request.getSourceIds())) {
                request.setSourceIds(ids);
            }
            relationshipEdgeService.saveBatch(request);
        }
    }

    public void batchRelateDemand(TestCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestCaseMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        String demandId = request.getDemandId();
        String demandName = request.getDemandName();
        if (StringUtils.isBlank(demandId) || (StringUtils.equals(demandId, "other") && StringUtils.isBlank(demandName))) {
            return;
        }
        if (!StringUtils.equals(demandId, "other")) {
            demandName = StringUtils.EMPTY;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper mapper = sqlSession.getMapper(TestCaseMapper.class);
        TestCaseExample example = new TestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        List<TestCase> testCaseList = testCaseMapper.selectByExample(example);
        Project project = null;
        if (CollectionUtils.isNotEmpty(testCaseList)) {
            project = baseProjectService.getProjectById(testCaseList.get(0).getProjectId());
        }

        for (TestCase tc : testCaseList) {

            if (project != null && StringUtils.equals(project.getPlatform(), IssuesManagePlatform.AzureDevops.name())) {
                EditTestCaseRequest editTestCaseRequest = new EditTestCaseRequest();
                BeanUtils.copyBean(editTestCaseRequest, tc);
                try {
                    doUpdateThirdPartyIssuesLink(editTestCaseRequest, project);
                    // 同步用例与需求的关联关系
                    IssuesRequest updateRequest = getIssuesRequest(editTestCaseRequest);
                    doAddDemandHyperLink(editTestCaseRequest, "edit", updateRequest, project);
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }

            tc.setDemandId(demandId);
            tc.setDemandName(demandName);
            mapper.updateByPrimaryKey(tc);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<TestCaseCountChartResult> countTestCaseByRequest(TestCaseCountRequest request) {
        return extTestCaseCountMapper.getFunctionCaseCount(request);
    }

    public List<TestAnalysisChartResult> countTestCaseByTypeAndRequest(String type, TestAnalysisChartRequest request) {
        if (StringUtils.equalsIgnoreCase(type, "create")) {
            return extTestAnalysisMapper.getCreateCaseReport(request);
        } else if (StringUtils.equalsIgnoreCase(type, "update")) {
            return extTestAnalysisMapper.getUpdateCaseReport(request);
        } else {
            return new ArrayList<>(0);
        }
    }

    public List<TestCase> selectByIds(QueryTestCaseRequest request) {
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            TestCaseExample example = new TestCaseExample();
            example.createCriteria().andIdIn(request.getIds());
            return testCaseMapper.selectByExample(example);
        } else {
            return new ArrayList<>(0);
        }
    }

    public TestCaseWithBLOBs getSimpleCaseForEdit(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseMapper.selectByPrimaryKey(testCaseId);
        Project project = baseProjectService.getProjectById(testCase.getProjectId());
        if (!SessionUtils.hasPermission(project.getWorkspaceId(), project.getId(), PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)) {
            MSException.throwException(Translator.get("check_owner_project"));
        }
        return testCaseMapper.selectByPrimaryKey(testCaseId);
    }

    public TestCaseWithBLOBs getSimpleCase(String testCaseId) {
        return testCaseMapper.selectByPrimaryKey(testCaseId);
    }
}
