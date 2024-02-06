package io.metersphere.plan.controller;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.plan.service.TestPlanModuleService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.TestPlanTestService;
import io.metersphere.plan.utils.TestPlanTestUtils;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.filemanagement.request.FileModuleCreateRequest;
import io.metersphere.project.dto.filemanagement.request.FileModuleUpdateRequest;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestPlanModule;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.CheckLogModel;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 1-10: 测试计划模块的增、改、查
 * 11-20：测试计划的增、改、查
 * 21-90：
 * ·测试计划相关资源的处理（功能用例、接口、场景、缺陷、以及预留的性能和UI）
 * ·测试计划关注
 * ·测试计划定时任务
 * 91-100：测试计划模块的移动、测试计划的移动（企业版，是否需要些暂定）
 * 101往后：测试计划的删除、测试计划模块的删除
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanTests extends BaseTest {
    private static Project project;

    private static List<BaseTreeNode> preliminaryTreeNodes = new ArrayList<>();

    @Resource
    private TestPlanModuleMapper testPlanModuleMapper;
    @Resource
    private TestPlanModuleService testPlanModuleService;
    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private TestPlanTestService testPlanTestService;

    private static final List<CheckLogModel> LOG_CHECK_LIST = new ArrayList<>();

    private static final List<FunctionalCase> FUNCTIONAL_CASES = new ArrayList<>();

    private static final List<ApiTestCase> API_TEST_CASES = new ArrayList<>();

    private static final List<ApiScenario> API_SCENARIOS = new ArrayList<>();

    //测试计划模块的url
    private static final String URL_GET_MODULE_TREE = "/test-plan/module/tree/%s";
    private static final String URL_GET_MODULE_DELETE = "/test-plan/module/delete/%s";
    private static final String URL_POST_MODULE_ADD = "/test-plan/module/add";
    private static final String URL_POST_MODULE_UPDATE = "/test-plan/module/update";
    private static final String URL_POST_MODULE_MOVE = "/test-plan/module/move";

    //测试计划url
    private static final String URL_GET_TEST_PLAN_COUNT = "/test-plan/getCount/%s";
    private static final String URL_GET_TEST_PLAN_DELETE = "/test-plan/delete/%s";
    private static final String URL_POST_TEST_PLAN_PAGE = "/test-plan/page";
    private static final String URL_POST_TEST_PLAN_MODULE_COUNT = "/test-plan/module/count";
    private static final String URL_POST_TEST_PLAN_ADD = "/test-plan/add";
    private static final String URL_POST_TEST_PLAN_UPDATE = "/test-plan/update";
    private static final String URL_POST_TEST_PLAN_BATCH_DELETE = "/test-plan/batch-delete";

    //测试计划资源-功能用例
    private static final String URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION = "/test-plan/functional/case/association";
    private static final String URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT = "/test-plan/functional/case/sort";

    //测试计划资源-接口用例
    private static final String URL_POST_RESOURCE_API_CASE_ASSOCIATION = "/test-plan/api/case/association";
    private static final String URL_POST_RESOURCE_API_CASE_SORT = "/test-plan/api/case/sort";

    //测试计划资源-场景用例
    private static final String URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION = "/test-plan/api/scenario/association";
    private static final String URL_POST_RESOURCE_API_SCENARIO_SORT = "/test-plan/api/scenario/sort";

    private static String groupTestPlanId7 = null;
    private static String groupTestPlanId15 = null;

    //普通测试计划
    private static TestPlan simpleTestPlan;
    //允许重复添加用例的测试计划
    private static TestPlan repeatCaseTestPlan;

    private static final String[] PROJECT_MODULE = new String[]{"workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"};

    @BeforeEach
    public void initTestData() {
        //文件管理专用项目
        if (project == null) {
            AddProjectRequest initProject = new AddProjectRequest();
            initProject.setOrganizationId("100001");
            initProject.setName("文件管理专用项目");
            initProject.setDescription("建国创建的文件管理专用项目");
            initProject.setEnable(true);
            initProject.setUserIds(List.of("admin"));
            project = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);


            //测试没指定module的项目检查模块菜单是否会报错
            boolean methodHasError = false;
            try {
                testPlanManagementService.checkModuleIsOpen(project.getId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, new ArrayList<>());
            } catch (Exception e) {
                methodHasError = true;
            }
            Assertions.assertTrue(methodHasError);
            //测试完之后，赋值module
            testPlanTestService.resetProjectModule(project, PROJECT_MODULE);
        }
    }


    private static long a1NodeCount = 0;
    private static long a2NodeCount = 0;
    private static long a3NodeCount = 0;
    private static long a1a1NodeCount = 0;
    private static long a1b1NodeCount = 0;

    @Test
    @Order(1)
    public void emptyDataTest() throws Exception {
        //空数据下，检查模块树
        List<BaseTreeNode> treeNodes = this.getFileModuleTreeNode();
        //检查有没有默认节点
        boolean hasNode = false;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getId(), ModuleConstants.DEFAULT_NODE_ID)) {
                hasNode = true;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertTrue(hasNode);

        //查询测试计划列表
        TestPlanTableRequest testPlanTableRequest = new TestPlanTableRequest();
        testPlanTableRequest.setProjectId(project.getId());

        //先测试一下没有开启模块时接口能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
        this.requestPost(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest).andExpect(status().is5xxServerError());
        this.requestGet(String.format(URL_GET_MODULE_TREE, project.getId())).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        MvcResult pageResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
        String returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<Object> result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(result.getCurrent(), testPlanTableRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= testPlanTableRequest.getPageSize());

        //判断权限
        this.requestGetPermissionTest(PermissionConstants.TEST_PLAN_MODULE_READ, String.format(URL_GET_MODULE_TREE, DEFAULT_PROJECT_ID));
        testPlanTableRequest.setProjectId(DEFAULT_PROJECT_ID);
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ, URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
    }

    @Test
    @Order(2)
    public void addModuleTest() throws Exception {
        //根目录下创建节点(a1）
        FileModuleCreateRequest request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");

        //测试项目没有开启测试计划模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, request);
        String returnId = mvcResult.getResponse().getContentAsString();
        Assertions.assertNotNull(returnId);
        List<BaseTreeNode> treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), request.getName())) {
                a1Node = baseTreeNode;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertNotNull(a1Node);
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1Node.getId(), OperationLogType.ADD, URL_POST_MODULE_ADD)
        );

        //根目录下创建节点a2和a3，在a1下创建子节点a1-b1
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a2");
        this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, request);

        request.setName("a3");
        this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, request);

        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, request);

        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1b1Node = null;
        BaseTreeNode a2Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(childNode.getName(), "a1-b1")) {
                        a1b1Node = childNode;
                    }
                    Assertions.assertNotNull(childNode.getParentId());
                }
            } else if (StringUtils.equals(baseTreeNode.getName(), "a2")) {
                a2Node = baseTreeNode;
            }
        }
        Assertions.assertNotNull(a2Node);
        Assertions.assertNotNull(a1b1Node);

        LOG_CHECK_LIST.add(
                new CheckLogModel(a2Node.getId(), OperationLogType.ADD, URL_POST_MODULE_ADD)
        );
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1b1Node.getId(), OperationLogType.ADD, URL_POST_MODULE_ADD)
        );

        //a1节点下可以继续添加a1节点
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, request);

        //继续创建a1下继续创建a1-a1-b1,
        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1ChildNode = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
                    Assertions.assertNotNull(childNode.getParentId());
                    if (StringUtils.equals(childNode.getName(), "a1")) {
                        a1ChildNode = childNode;
                    }
                }
            }
        }
        Assertions.assertNotNull(a1ChildNode);
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1ChildNode.getId(), OperationLogType.ADD, URL_POST_MODULE_ADD)
        );

        //a1的子节点a1下继续创建节点a1-a1-c1
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-a1-c1");
        request.setParentId(a1ChildNode.getId());
        this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, request);
        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1a1c1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    Assertions.assertNotNull(secondNode.getParentId());
                    if (StringUtils.equals(secondNode.getName(), "a1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
                            Assertions.assertNotNull(thirdNode.getParentId());
                            if (StringUtils.equals(thirdNode.getName(), "a1-a1-c1")) {
                                a1a1c1Node = thirdNode;
                            }
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(a1a1c1Node);
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1a1c1Node.getId(), OperationLogType.ADD, URL_POST_MODULE_ADD)
        );
        //子节点a1-b1下继续创建节点a1-b1-c1
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1-c1");
        request.setParentId(a1b1Node.getId());
        this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, request);
        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1b1c1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(secondNode.getName(), "a1-b1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
                            if (StringUtils.equals(thirdNode.getName(), "a1-b1-c1")) {
                                a1b1c1Node = thirdNode;
                            }
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(a1b1c1Node);
        preliminaryTreeNodes = treeNodes;

        LOG_CHECK_LIST.add(
                new CheckLogModel(a1b1c1Node.getId(), OperationLogType.ADD, URL_POST_MODULE_ADD)
        );


        /*
         测试能否正常做200个节点
         */
        String parentId = null;
        for (int i = 0; i < 210; i++) {
            FileModuleCreateRequest perfRequest = new FileModuleCreateRequest();
            perfRequest.setProjectId(project.getId());
            perfRequest.setName("500-test-root-" + i);
            if (StringUtils.isNotEmpty(parentId)) {
                perfRequest.setParentId(parentId);
            }
            if (i < 200) {
                MvcResult result = this.requestPostWithOkAndReturn(URL_POST_MODULE_ADD, perfRequest);
                ResultHolder holder = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class);
                if (i % 50 == 0) {
                    //到20换下一层级
                    parentId = holder.getData().toString();
                }
            }
        }
        treeNodes = this.getFileModuleTreeNode();
        preliminaryTreeNodes = treeNodes;

        a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1");
        assert a1Node != null;

        //参数校验
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().isBadRequest());
        request = new FileModuleCreateRequest();
        request.setName("none");
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().isBadRequest());
        request = new FileModuleCreateRequest();
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().isBadRequest());
        request = new FileModuleCreateRequest();
        request.setParentId(null);
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().isBadRequest());

        //父节点ID不存在的
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("ParentIsUUID");
        request.setParentId(IDGenerator.nextStr());
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //添加重复的a1节点
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //a1节点下添加重复的a1-b1节点
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //子节点的项目ID和父节点的不匹配
        request = new FileModuleCreateRequest();
        request.setProjectId(IDGenerator.nextStr());
        request.setName("RandomUUID");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //项目ID和父节点的不匹配
        request = new FileModuleCreateRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("RandomUUID");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_POST_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //判断权限
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_MODULE_READ_ADD, URL_POST_MODULE_ADD, request);
    }

    @Test
    @Order(3)
    public void updateModuleTest() throws Exception {
        if (CollectionUtils.isEmpty(preliminaryTreeNodes)) {
            this.addModuleTest();
        }
        //更改名称
        BaseTreeNode a1Node = null;
        for (BaseTreeNode node : preliminaryTreeNodes) {
            if (StringUtils.equals(node.getName(), "a1")) {
                for (BaseTreeNode a1ChildrenNode : node.getChildren()) {
                    if (StringUtils.equals(a1ChildrenNode.getName(), "a1")) {
                        a1Node = a1ChildrenNode;
                    }
                }
            }
        }
        assert a1Node != null;
        FileModuleUpdateRequest updateRequest = new FileModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-a1");

        //测试项目没有开启测试计划模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
        this.requestPost(URL_POST_MODULE_UPDATE, updateRequest).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        this.requestPostWithOkAndReturn(URL_POST_MODULE_UPDATE, updateRequest);

        preliminaryTreeNodes = this.getFileModuleTreeNode();
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1Node.getId(), OperationLogType.UPDATE, URL_POST_MODULE_UPDATE)
        );

        a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        assert a1Node != null;
        //反例-参数校验
        updateRequest = new FileModuleUpdateRequest();
        this.requestPost(URL_POST_MODULE_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //id不存在
        updateRequest = new FileModuleUpdateRequest();
        updateRequest.setId(IDGenerator.nextStr());
        updateRequest.setName(IDGenerator.nextStr());
        this.requestPost(URL_POST_MODULE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

        //名称重复   a1-a1改为a1-b1
        updateRequest = new FileModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-b1");
        this.requestPost(URL_POST_MODULE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

        //判断权限
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_MODULE_READ_UPDATE, URL_POST_MODULE_UPDATE, updateRequest);
    }


    @Test
    @Order(11)
    public void testPlanAddTest() throws Exception {
        this.preliminaryTree();

        BaseTreeNode a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1");
        BaseTreeNode a2Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a2");
        BaseTreeNode a3Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a3");
        BaseTreeNode a1a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        BaseTreeNode a1b1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-b1");

        assert a1Node != null & a2Node != null & a3Node != null & a1a1Node != null & a1b1Node != null;
        TestPlanCreateRequest request = new TestPlanCreateRequest();
        request.setProjectId(project.getId());

        for (int i = 0; i < 999; i++) {
            String moduleId;
            if (i < 50) {
                moduleId = a1Node.getId();
                //选择安东尼巅峰期球衣号码为标志，该测试计划将改为测试计划组，并用于后续的测试用例相关操作
                if (i == 7 || i == 15) {
                    request.setType(TestPlanConstants.TEST_PLAN_TYPE_GROUP);
                }
                a1NodeCount++;
            } else if (i < 100) {
                moduleId = a2Node.getId();
                request.setPlannedStartTime(System.currentTimeMillis());
                request.setPlannedEndTime(System.currentTimeMillis() + 20000);
                a2NodeCount++;
            } else if (i < 150) {
                moduleId = a3Node.getId();
                request.setRepeatCase(true);
                request.setAutomaticStatusUpdate(true);
                a3NodeCount++;
            } else if (i < 200) {
                moduleId = a1a1Node.getId();
                request.setPassThreshold((double) i / 3);
                request.setDescription("test plan desc " + i);
                a1a1NodeCount++;
            } else {
                moduleId = a1b1Node.getId();
                a1b1NodeCount++;
            }

            if (i == 201) {
                //第201时，先测试能否添加重复用例
                request.setName("testPlan_200");
                request.setModuleId(moduleId);
                this.requestPost(URL_POST_TEST_PLAN_ADD, request).andExpect(status().is5xxServerError());
            }
            //添加测试计划
            request.setName("testPlan_" + i);
            request.setModuleId(moduleId);
            MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_ADD, request);
            String returnStr = mvcResult.getResponse().getContentAsString();
            ResultHolder holder = JSON.parseObject(returnStr, ResultHolder.class);
            String returnId = holder.getData().toString();
            Assertions.assertNotNull(returnId);

            if (i == 7) {
                groupTestPlanId7 = returnId;
            } else if (i == 15) {
                groupTestPlanId15 = returnId;
            }

            //操作日志检查
            LOG_CHECK_LIST.add(
                    new CheckLogModel(returnId, OperationLogType.ADD, URL_POST_TEST_PLAN_ADD)
            );

            //部分请求的参数初始化
            request.setPlannedEndTime(null);
            request.setPlannedStartTime(null);
            request.setRepeatCase(false);
            request.setAutomaticStatusUpdate(false);
            request.setPassThreshold(100);
            request.setDescription(null);
            request.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
        }


        /*
        抽查：
            testPlan_13没有设置计划开始时间、没有设置重复添加用例和自动更新状态、阈值为100、描述为空；
            testPlan_53检查是否设置了计划开始结束时间；
            testPlan_123是否设置了重复添加用例和自动更新状态；
            testPlan_173的阈值是否不等于100、描述不会为空
         */
        testPlanTestService.checkTestPlanByAddTest();
        simpleTestPlan = testPlanTestService.selectTestPlanByName("testPlan_13");
        repeatCaseTestPlan = testPlanTestService.selectTestPlanByName("testPlan_123");
        //测试继续创建10个
        for (int i = 0; i < 10; i++) {
            request.setName("testPlan_1000_" + i);
            this.requestPost(URL_POST_TEST_PLAN_ADD, request).andExpect(status().is5xxServerError());
        }

        //在groupTestPlanId7、groupTestPlanId15下面各创建20条数据
        for (int i = 0; i < 20; i++) {
            TestPlanCreateRequest itemRequest = new TestPlanCreateRequest();
            itemRequest.setProjectId(project.getId());
            itemRequest.setModuleId(a1Node.getId());
            itemRequest.setGroupId(groupTestPlanId7);
            itemRequest.setName("testPlan_group7_" + i);
            if (i == 0) {
                //测试项目没有开启测试计划模块时能否使用
                testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
                this.requestPost(URL_POST_TEST_PLAN_ADD, itemRequest).andExpect(status().is5xxServerError());
                //恢复
                testPlanTestService.resetProjectModule(project, PROJECT_MODULE);
            }
            MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_ADD, itemRequest);
            String returnStr = mvcResult.getResponse().getContentAsString();
            ResultHolder holder = JSON.parseObject(returnStr, ResultHolder.class);
            Assertions.assertNotNull(holder.getData().toString());

            itemRequest.setGroupId(groupTestPlanId15);
            itemRequest.setName("testPlan_group15_" + i);
            mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_ADD, itemRequest);
            returnStr = mvcResult.getResponse().getContentAsString();
            holder = JSON.parseObject(returnStr, ResultHolder.class);
            Assertions.assertNotNull(holder.getData().toString());
        }

        /*
        反例
            1.参数校验： name为空
            2.module_id不存在
            3.group_id
                3.1 group_id不存在
                3.2 group_id对应的测试计划type不是group
            4.参数校验：passThreshold大于100 、 小于0
            5.重名校验
        */
        request.setName(null);
        this.requestPost(URL_POST_TEST_PLAN_ADD, request).andExpect(status().isBadRequest());
        request.setName(IDGenerator.nextStr());
        request.setModuleId(IDGenerator.nextStr());
        this.requestPost(URL_POST_TEST_PLAN_ADD, request).andExpect(status().is5xxServerError());
        request.setModuleId(a1Node.getId());
        request.setGroupId(testPlanTestService.selectTestPlanByName("testPlan_60").getGroupId());
        this.requestPost(URL_POST_TEST_PLAN_ADD, request).andExpect(status().is5xxServerError());
        request.setGroupId(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
        request.setPassThreshold(100.111);
        this.requestPost(URL_POST_TEST_PLAN_ADD, request).andExpect(status().isBadRequest());
        request.setPassThreshold(-0.12);
        this.requestPost(URL_POST_TEST_PLAN_ADD, request).andExpect(status().isBadRequest());

        //测试权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPassThreshold(100);
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_ADD, URL_POST_TEST_PLAN_ADD, request);

    }

    @Test
    @Order(12)
    public void testPlanPageCountTest() throws Exception {
        TestPlanTableRequest testPlanTableRequest = new TestPlanTableRequest();
        testPlanTableRequest.setProjectId(project.getId());

        //测试项目没有开启测试计划模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
        this.requestPost(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest).andExpect(status().is5xxServerError());
        this.requestPost(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        MvcResult moduleCountResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest);
        String moduleCountReturnData = moduleCountResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Map<String, Object> moduleCountMap = JSON.parseObject(JSON.toJSONString(JSON.parseObject(moduleCountReturnData, ResultHolder.class).getData()), Map.class);
        AtomicBoolean testPlanIsEmpty = new AtomicBoolean(true);
        for (Map.Entry<String, Object> entry : moduleCountMap.entrySet()) {
            long value = Long.parseLong(String.valueOf(entry.getValue()));
            if (value > 0) {
                testPlanIsEmpty.set(false);
            }
        }

        if (testPlanIsEmpty.get()) {
            //如果没有数据，先创建999条再调用这个方法
            this.testPlanAddTest();
            this.testPlanPageCountTest();
        } else {
            this.checkModuleCount(moduleCountMap, a1NodeCount, a2NodeCount, a3NodeCount, a1a1NodeCount, a1b1NodeCount);

            BaseTreeNode a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1");
            BaseTreeNode a2Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a2");
            BaseTreeNode a3Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a3");
            BaseTreeNode a1a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
            BaseTreeNode a1b1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-b1");
            assert a1Node != null & a2Node != null & a3Node != null & a1a1Node != null & a1b1Node != null;

            //查询测试计划列表
            MvcResult pageResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
            String returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            Pager<Object> result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
            //返回值的页码和当前页码相同
            Assertions.assertEquals(result.getCurrent(), testPlanTableRequest.getCurrent());
            //返回的数据量不超过规定要返回的数据量相同
            Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= testPlanTableRequest.getPageSize());
            Assertions.assertEquals(result.getTotal(), 999);

            //按照名称倒叙
            testPlanTableRequest.setSort(new HashMap<>() {{
                this.put("name", "desc");
            }});
            pageResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
            returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
            //返回值的页码和当前页码相同
            Assertions.assertEquals(result.getCurrent(), testPlanTableRequest.getCurrent());
            //返回的数据量不超过规定要返回的数据量相同
            Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= testPlanTableRequest.getPageSize());
            Assertions.assertEquals(result.getTotal(), 999);

            //查询详情
            List<TestPlanResponse> testPlanResponseList = JSON.parseArray(JSON.toJSONString(result.getList()), TestPlanResponse.class);
            for (TestPlanResponse response : testPlanResponseList) {
                this.requestGetWithOk(String.format(URL_GET_TEST_PLAN_COUNT, response.getId()));
            }

            //指定模块ID查询 (查询count时，不会因为选择了模块而更改了总量
            testPlanTableRequest.setModuleIds(Arrays.asList(a1Node.getId(), a1a1Node.getId(), a1b1Node.getId()));
            moduleCountResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest);
            moduleCountReturnData = moduleCountResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            moduleCountMap = JSON.parseObject(JSON.toJSONString(JSON.parseObject(moduleCountReturnData, ResultHolder.class).getData()), Map.class);
            this.checkModuleCount(moduleCountMap, a1NodeCount, a2NodeCount, a3NodeCount, a1a1NodeCount, a1b1NodeCount);

            pageResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
            returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
            //返回值的页码和当前页码相同
            Assertions.assertEquals(result.getCurrent(), testPlanTableRequest.getCurrent());
            //返回的数据量不超过规定要返回的数据量相同
            Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= testPlanTableRequest.getPageSize());
            Assertions.assertEquals(result.getTotal(), 899);


            //测试根据名称模糊查询： Plan_2  预期结果： a1Node下有11条（testPlan_2,testPlan_20~testPlan_29), a1b1Node下有100条（testPlan_200~testPlan_299）
            testPlanTableRequest.setModuleIds(null);
            testPlanTableRequest.setKeyword("Plan_2");
            moduleCountResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest);
            moduleCountReturnData = moduleCountResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            moduleCountMap = JSON.parseObject(JSON.toJSONString(JSON.parseObject(moduleCountReturnData, ResultHolder.class).getData()), Map.class);
            this.checkModuleCount(moduleCountMap, 11, 0, 0, 0, 100);

            pageResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
            returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
            //返回值的页码和当前页码相同
            Assertions.assertEquals(result.getCurrent(), testPlanTableRequest.getCurrent());
            //返回的数据量不超过规定要返回的数据量相同
            Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= testPlanTableRequest.getPageSize());
            Assertions.assertEquals(result.getTotal(), 111);


            //测试根据名称模糊查询（包含测试组的）： Plan_7  预期结果： a1Node下有1条（testPlan_7), a2Node下有10条（testPlan_70~testPlan_79）,a1b1Node下有100条（testPlan_700~testPlan_799）
            testPlanTableRequest.setKeyword("Plan_7");
            testPlanTableRequest.setSort(new HashMap<>() {{
                this.put("num", "asc");
            }});
            moduleCountResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest);
            moduleCountReturnData = moduleCountResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            moduleCountMap = JSON.parseObject(JSON.toJSONString(JSON.parseObject(moduleCountReturnData, ResultHolder.class).getData()), Map.class);
            this.checkModuleCount(moduleCountMap, 1, 10, 0, 0, 100);

            pageResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
            returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
            //返回值的页码和当前页码相同
            Assertions.assertEquals(result.getCurrent(), testPlanTableRequest.getCurrent());
            //返回的数据量不超过规定要返回的数据量相同
            Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= testPlanTableRequest.getPageSize());
            Assertions.assertEquals(result.getTotal(), 111);

            //反例：参数校验（项目ID不存在）
            testPlanTableRequest.setProjectId(null);
            this.requestPost(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest).andExpect(status().isBadRequest());
            this.requestPost(URL_POST_TEST_PLAN_PAGE, testPlanTableRequest).andExpect(status().isBadRequest());

            //测试权限
            testPlanTableRequest.setProjectId(DEFAULT_PROJECT_ID);
            this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ, URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest);
            this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ, URL_POST_TEST_PLAN_PAGE, testPlanTableRequest);
        }

    }


    @Test
    @Order(13)
    public void testPlanUpdateTest() throws Exception {
        if (StringUtils.isAnyBlank(groupTestPlanId7, groupTestPlanId15)) {
            this.testPlanAddTest();
        }
        TestPlan testPlan = testPlanTestService.selectTestPlanByName("testPlan_21");
        TestPlanConfig testPlanConfig = testPlanTestService.selectTestPlanConfigById(testPlan.getId());

        //修改名称
        TestPlanUpdateRequest updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setName(IDGenerator.nextStr());

        //测试项目没有开启测试计划模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
        this.requestPost(URL_POST_TEST_PLAN_UPDATE, updateRequest).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        String returnStr = mvcResult.getResponse().getContentAsString();
        ResultHolder holder = JSON.parseObject(returnStr, ResultHolder.class);
        String returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);
        //操作日志检查
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.UPDATE, URL_POST_TEST_PLAN_UPDATE)
        );

        //修改回来
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setName(testPlan.getName());
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);

        //修改模块
        BaseTreeNode a2Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a2");
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setModuleId(a2Node.getId());
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);
        //修改回来
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setModuleId(testPlan.getModuleId());
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);

        //修改标签
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setTags(new LinkedHashSet<>(Arrays.asList("tag1", "tag2", "tag3", "tag3")));
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);
        testPlan = testPlanTestService.selectTestPlanByName("testPlan_21");

        //修改计划开始结束时间
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setPlannedStartTime(System.currentTimeMillis());
        updateRequest.setPlannedEndTime(updateRequest.getPlannedStartTime() - 10000);
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);
        testPlan = testPlanTestService.selectTestPlanByName("testPlan_21");

        //修改描述
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setDescription("This is desc");
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);
        testPlan = testPlanTestService.selectTestPlanByName("testPlan_21");

        //修改配置项
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setAutomaticStatusUpdate(true);
        updateRequest.setRepeatCase(true);
        updateRequest.setPassThreshold(43.123);
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);

        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setAutomaticStatusUpdate(false);
        updateRequest.setRepeatCase(false);
        updateRequest.setPassThreshold(56.478);
        mvcResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        returnStr = mvcResult.getResponse().getContentAsString();
        holder = JSON.parseObject(returnStr, ResultHolder.class);
        returnId = holder.getData().toString();
        Assertions.assertEquals(returnId, testPlan.getId());
        testPlanTestService.checkTestPlanUpdateResult(testPlan, testPlanConfig, updateRequest);
        testPlan = testPlanTestService.selectTestPlanByName("testPlan_21");
        testPlanConfig = testPlanTestService.selectTestPlanConfigById(testPlan.getId());

        //修改a2节点下的数据（91,92）的所属测试计划组
        updateRequest = testPlanTestService.generateUpdateRequest(testPlanTestService.selectTestPlanByName("testPlan_91").getId());
        updateRequest.setTestPlanGroupId(groupTestPlanId7);
        this.requestPostWithOk(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        a2NodeCount--;
        updateRequest = testPlanTestService.generateUpdateRequest(testPlanTestService.selectTestPlanByName("testPlan_92").getId());
        updateRequest.setTestPlanGroupId(groupTestPlanId7);
        this.requestPostWithOk(URL_POST_TEST_PLAN_UPDATE, updateRequest);
        a2NodeCount--;

        //修改测试计划组信息
        updateRequest = testPlanTestService.generateUpdateRequest(groupTestPlanId7);
        updateRequest.setName(IDGenerator.nextStr());
        updateRequest.setDescription("This is desc");
        updateRequest.setTags(new LinkedHashSet<>(Arrays.asList("tag1", "tag2", "tag3", "tag3")));
        updateRequest.setPlannedStartTime(System.currentTimeMillis());
        updateRequest.setPlannedEndTime(updateRequest.getPlannedStartTime() - 10000);
        this.requestPostWithOk(URL_POST_TEST_PLAN_UPDATE, updateRequest);

        //什么都不修改
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        this.requestPostWithOk(URL_POST_TEST_PLAN_UPDATE, updateRequest);

        //反例：名称重复
        updateRequest.setName("testPlan_400");
        this.requestPost(URL_POST_TEST_PLAN_UPDATE, updateRequest).andExpect(status().is5xxServerError());

        //因为有条数据被移动了测试计划组里，所以检查一下moduleCount.
        TestPlanTableRequest testPlanTableRequest = new TestPlanTableRequest();
        testPlanTableRequest.setProjectId(project.getId());
        MvcResult moduleCountResult = this.requestPostWithOkAndReturn(URL_POST_TEST_PLAN_MODULE_COUNT, testPlanTableRequest);
        String moduleCountReturnData = moduleCountResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Map<String, Object> moduleCountMap = JSON.parseObject(JSON.toJSONString(JSON.parseObject(moduleCountReturnData, ResultHolder.class).getData()), Map.class);
        this.checkModuleCount(moduleCountMap, a1NodeCount, a2NodeCount, a3NodeCount, a1a1NodeCount, a1b1NodeCount);
        //反例：不写id
        updateRequest = new TestPlanUpdateRequest();
        this.requestPost(URL_POST_TEST_PLAN_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //反例：阈值小于0
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setPassThreshold(-1.2);
        this.requestPost(URL_POST_TEST_PLAN_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //反例：阈值大于100
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setPassThreshold(100.2);
        this.requestPost(URL_POST_TEST_PLAN_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //反例：id是错的
        updateRequest = testPlanTestService.generateUpdateRequest(testPlan.getId());
        updateRequest.setId(IDGenerator.nextStr());
        this.requestPost(URL_POST_TEST_PLAN_UPDATE, updateRequest).andExpect(status().is5xxServerError());

    }

    @Test
    @Order(21)
    public void testPlanAssociationFunctionCase() throws Exception {
        if (ObjectUtils.anyNull(simpleTestPlan, repeatCaseTestPlan)) {
            this.testPlanAddTest();
        }
        //创建20个功能测试用例
        FUNCTIONAL_CASES.addAll(testPlanTestService.createFunctionCase(20, project.getId()));

        //测试不开启用例重复的测试计划多次关联
        TestPlanAssociationRequest request = new TestPlanAssociationRequest();
        request.setTestPlanId(simpleTestPlan.getId());
        request.setSelectIds(FUNCTIONAL_CASES.stream().map(FunctionalCase::getId).collect(Collectors.toList()));
        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanAssociationResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), FUNCTIONAL_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE), FUNCTIONAL_CASES.size());

        //先测试一下没有开启模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "caseManagement");
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), 0);
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE), FUNCTIONAL_CASES.size());

        //测试开启用例重复的测试计划多次关联
        request.setTestPlanId(repeatCaseTestPlan.getId());
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), FUNCTIONAL_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE), FUNCTIONAL_CASES.size() * 1);

        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), FUNCTIONAL_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE), FUNCTIONAL_CASES.size() * 2);

        //同时测试名称排序
        request.setOrderColumn("name");
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), FUNCTIONAL_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE), FUNCTIONAL_CASES.size() * 3);

        request.setOrderByAsc(false);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), FUNCTIONAL_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE), FUNCTIONAL_CASES.size() * 4);

        //反例 测试计划ID为空
        request.setTestPlanId(null);
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request).andExpect(status().isBadRequest());
        //反例 测试计划不存在
        request.setTestPlanId(IDGenerator.nextStr());
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request).andExpect(status().is5xxServerError());

        //测试权限
        request.setTestPlanId(simpleTestPlan.getId());
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_ASSOCIATION, URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION, request);

        LOG_CHECK_LIST.add(
                new CheckLogModel(simpleTestPlan.getId(), OperationLogType.ADD, URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION)
        );
        LOG_CHECK_LIST.add(
                new CheckLogModel(repeatCaseTestPlan.getId(), OperationLogType.ADD, URL_POST_RESOURCE_FUNCTIONAL_CASE_ASSOCIATION)
        );
    }

    @Test
    @Order(22)
    public void testPlanFunctionCaseSort() throws Exception {
        if (FUNCTIONAL_CASES.size() == 0) {
            this.testPlanAssociationFunctionCase();
        }
        List<TestPlanFunctionalCase> funcList = testPlanTestService.selectTestPlanFunctionalCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertTrue(funcList.size() >= FUNCTIONAL_CASES.size() * 4);
        //将第80个移动到第一位之前
        ResourceSortRequest request = new ResourceSortRequest();
        request.setTestPlanId(repeatCaseTestPlan.getId());
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(-1);

        //先测试一下没有开启模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "caseManagement");
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanResourceSortResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanFunctionalCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDragNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDropNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT)
        );

        //将这时的第80个放到第一位之后
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(1);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanFunctionalCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDropNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDragNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT)
        );


        //再将这时的第80个放到第一位之后,但是第一个的pos为2，检查能否触发ref操作
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(-1);
        testPlanTestService.setResourcePos(funcList.get(0).getId(), TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, 2);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanFunctionalCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDragNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDropNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT)
        );

        //反例：测试计划为空
        request.setTestPlanId(null);
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().isBadRequest());
        //反例： 测试计划不存在
        request.setTestPlanId(IDGenerator.nextStr());
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例：拖拽的节点不存在
        request.setTestPlanId(repeatCaseTestPlan.getId());
        request.setDragNodeId(null);
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例：目标节点不存在
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(null);
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例： 节点重复
        request.setDropNodeId(request.getDragNodeId());
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例： dropPosition取值范围不对
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(0);
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().is5xxServerError());

        //测试权限
        request.setDropPosition(1);
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_UPDATE, URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request);

    }


    @Test
    @Order(31)
    public void testPlanAssociationApiTestCase() throws Exception {
        this.preliminaryTestPlan();
        //创建20个功能测试用例
        API_TEST_CASES.addAll(testPlanTestService.createApiCases(20, project.getId()));
        //测试不开启用例重复的测试计划多次关联
        TestPlanAssociationRequest request = new TestPlanAssociationRequest();
        request.setTestPlanId(simpleTestPlan.getId());
        request.setSelectIds(API_TEST_CASES.stream().map(ApiTestCase::getId).collect(Collectors.toList()));
        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_ASSOCIATION, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanAssociationResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_TEST_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_CASE), API_TEST_CASES.size());

        //先测试一下没有开启模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "apiTest");
        this.requestPost(URL_POST_RESOURCE_API_CASE_ASSOCIATION, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), 0);
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_CASE), API_TEST_CASES.size());

        //测试开启用例重复的测试计划多次关联
        request.setTestPlanId(repeatCaseTestPlan.getId());
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_TEST_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_CASE), API_TEST_CASES.size() * 1);

        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_TEST_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_CASE), API_TEST_CASES.size() * 2);

        //同时测试名称排序
        request.setOrderColumn("name");
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_TEST_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_CASE), API_TEST_CASES.size() * 3);

        request.setOrderByAsc(false);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_TEST_CASES.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_CASE), API_TEST_CASES.size() * 4);

        //反例 测试计划ID为空
        request.setTestPlanId(null);
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().isBadRequest());
        //反例 测试计划不存在
        request.setTestPlanId(IDGenerator.nextStr());
        this.requestPost(URL_POST_RESOURCE_FUNCTIONAL_CASE_SORT, request).andExpect(status().is5xxServerError());

        //测试权限
        request.setTestPlanId(simpleTestPlan.getId());
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_ASSOCIATION, URL_POST_RESOURCE_API_CASE_ASSOCIATION, request);

        LOG_CHECK_LIST.add(
                new CheckLogModel(simpleTestPlan.getId(), OperationLogType.ADD, URL_POST_RESOURCE_API_CASE_ASSOCIATION)
        );
        LOG_CHECK_LIST.add(
                new CheckLogModel(repeatCaseTestPlan.getId(), OperationLogType.ADD, URL_POST_RESOURCE_API_CASE_ASSOCIATION)
        );
    }

    @Test
    @Order(32)
    public void testPlanApiCaseSort() throws Exception {
        if (API_TEST_CASES.size() == 0) {
            this.testPlanAssociationFunctionCase();
        }
        List<TestPlanApiCase> funcList = testPlanTestService.selectTestPlanApiCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertTrue(funcList.size() >= API_TEST_CASES.size() * 4);
        //将第80个移动到第一位之前
        ResourceSortRequest request = new ResourceSortRequest();
        request.setTestPlanId(repeatCaseTestPlan.getId());
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(-1);

        //先测试一下没有开启模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "apiTest");
        this.requestPost(URL_POST_RESOURCE_API_CASE_SORT, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_SORT, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanResourceSortResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanApiCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDragNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDropNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_API_CASE_SORT)
        );

        //将这时的第80个放到第一位之后
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(1);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanApiCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDropNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDragNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_API_CASE_SORT)
        );

        //再将这时的第80个放到第一位之前,但是第一个的pos为2，检查能否触发ref操作
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(-1);
        testPlanTestService.setResourcePos(funcList.get(0).getId(), TestPlanResourceConstants.RESOURCE_API_CASE, 2);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanApiCaseByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDragNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDropNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_API_CASE_SORT)
        );

        //反例：测试计划为空
        request.setTestPlanId(null);
        this.requestPost(URL_POST_RESOURCE_API_CASE_SORT, request).andExpect(status().isBadRequest());
        //反例： 测试计划不存在
        request.setTestPlanId(IDGenerator.nextStr());
        this.requestPost(URL_POST_RESOURCE_API_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例：拖拽的节点不存在
        request.setTestPlanId(repeatCaseTestPlan.getId());
        request.setDragNodeId(null);
        this.requestPost(URL_POST_RESOURCE_API_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例：目标节点不存在
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(null);
        this.requestPost(URL_POST_RESOURCE_API_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例： 节点重复
        request.setDropNodeId(request.getDragNodeId());
        this.requestPost(URL_POST_RESOURCE_API_CASE_SORT, request).andExpect(status().is5xxServerError());
        //反例： dropPosition取值范围不对
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(0);
        this.requestPost(URL_POST_RESOURCE_API_CASE_SORT, request).andExpect(status().is5xxServerError());

        //测试权限
        request.setDropPosition(1);
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_UPDATE, URL_POST_RESOURCE_API_CASE_SORT, request);

    }


    @Test
    @Order(41)
    public void testPlanAssociationApiScenario() throws Exception {
        if (ObjectUtils.anyNull(simpleTestPlan, repeatCaseTestPlan)) {
            this.testPlanAddTest();
        }
        //创建20个功能测试用例
        API_SCENARIOS.addAll(testPlanTestService.createApiScenario(20, project.getId()));

        //测试不开启用例重复的测试计划多次关联
        TestPlanAssociationRequest request = new TestPlanAssociationRequest();
        request.setTestPlanId(simpleTestPlan.getId());
        request.setSelectIds(API_SCENARIOS.stream().map(ApiScenario::getId).collect(Collectors.toList()));
        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanAssociationResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_SCENARIOS.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_SCENARIO), API_SCENARIOS.size());

        //先测试一下没有开启模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "apiTest");
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), 0);
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_SCENARIO), API_SCENARIOS.size());

        //测试开启用例重复的测试计划多次关联
        request.setTestPlanId(repeatCaseTestPlan.getId());
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_SCENARIOS.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_SCENARIO), API_SCENARIOS.size() * 1);

        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_SCENARIOS.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_SCENARIO), API_SCENARIOS.size() * 2);

        //同时测试名称排序
        request.setOrderColumn("name");
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_SCENARIOS.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_SCENARIO), API_SCENARIOS.size() * 3);

        request.setOrderByAsc(false);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanAssociationResponse.class);
        Assertions.assertEquals(response.getAssociationCount(), API_SCENARIOS.size());
        Assertions.assertEquals(testPlanTestService.countResource(request.getTestPlanId(), TestPlanResourceConstants.RESOURCE_API_SCENARIO), API_SCENARIOS.size() * 4);

        //反例 测试计划ID为空
        request.setTestPlanId(null);
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request).andExpect(status().isBadRequest());
        //反例 测试计划不存在
        request.setTestPlanId(IDGenerator.nextStr());
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request).andExpect(status().is5xxServerError());

        //测试权限
        request.setTestPlanId(simpleTestPlan.getId());
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_ASSOCIATION, URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION, request);

        LOG_CHECK_LIST.add(
                new CheckLogModel(simpleTestPlan.getId(), OperationLogType.ADD, URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION)
        );
        LOG_CHECK_LIST.add(
                new CheckLogModel(repeatCaseTestPlan.getId(), OperationLogType.ADD, URL_POST_RESOURCE_API_SCENARIO_ASSOCIATION)
        );
    }

    @Test
    @Order(42)
    public void testPlanApiScenarioSort() throws Exception {
        if (API_SCENARIOS.size() == 0) {
            this.testPlanAssociationFunctionCase();
        }
        List<TestPlanApiScenario> funcList = testPlanTestService.selectTestPlanApiScenarioByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertTrue(funcList.size() >= API_SCENARIOS.size() * 4);
        //将第80个移动到第一位之前
        ResourceSortRequest request = new ResourceSortRequest();
        request.setTestPlanId(repeatCaseTestPlan.getId());
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(-1);

        //先测试一下没有开启模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "apiTest");
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_SORT, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_SORT, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanResourceSortResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanApiScenarioByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDragNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDropNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_API_SCENARIO_SORT)
        );

        //将这时的第80个放到第一位之后
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(1);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanApiScenarioByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDropNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDragNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_API_SCENARIO_SORT)
        );

        //再将这时的第80个放到第一位之后,但是第一个的pos为2，检查能否触发ref操作
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(-1);
        testPlanTestService.setResourcePos(funcList.get(0).getId(), TestPlanResourceConstants.RESOURCE_API_SCENARIO, 2);
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanResourceSortResponse.class);
        Assertions.assertEquals(response.getSortNodeNum(), 1);
        funcList = testPlanTestService.selectTestPlanApiScenarioByTestPlanId(repeatCaseTestPlan.getId());
        Assertions.assertEquals(funcList.get(0).getId(), request.getDragNodeId());
        Assertions.assertEquals(funcList.get(1).getId(), request.getDropNodeId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(request.getDragNodeId(), OperationLogType.UPDATE, URL_POST_RESOURCE_API_SCENARIO_SORT)
        );

        //反例：测试计划为空
        request.setTestPlanId(null);
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_SORT, request).andExpect(status().isBadRequest());
        //反例： 测试计划不存在
        request.setTestPlanId(IDGenerator.nextStr());
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_SORT, request).andExpect(status().is5xxServerError());
        //反例：拖拽的节点不存在
        request.setTestPlanId(repeatCaseTestPlan.getId());
        request.setDragNodeId(null);
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_SORT, request).andExpect(status().is5xxServerError());
        //反例：目标节点不存在
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(null);
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_SORT, request).andExpect(status().is5xxServerError());
        //反例： 节点重复
        request.setDropNodeId(request.getDragNodeId());
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_SORT, request).andExpect(status().is5xxServerError());
        //反例： dropPosition取值范围不对
        request.setDragNodeId(funcList.get(79).getId());
        request.setDropNodeId(funcList.get(0).getId());
        request.setDropPosition(0);
        this.requestPost(URL_POST_RESOURCE_API_SCENARIO_SORT, request).andExpect(status().is5xxServerError());

        //测试权限
        request.setDropPosition(1);
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_UPDATE, URL_POST_RESOURCE_API_SCENARIO_SORT, request);

    }


    @Test
    @Order(91)
    public void moveTest() throws Exception {
        this.preliminaryTree();
            /*
                *默认节点
                |
                ·a1 +
                |   |
                |   ·a1-b1   +
                |   |        |
                |   |        ·a1-b1-c1
                |   |
                |   *a1-a1   +（创建的时候是a1，通过修改改为a1-a1）
                |            |
                |            ·a1-a1-c1
                |
                ·a2
                |
                ·a3
            */
        BaseTreeNode a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1");
        BaseTreeNode a2Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a2");
        BaseTreeNode a3Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a3");
        BaseTreeNode a1a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        BaseTreeNode a1b1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-b1");
        assert a1Node != null & a2Node != null & a3Node != null & a1a1Node != null & a1b1Node != null;
        //父节点内移动-移动到首位 a1挪到a3后面
        NodeMoveRequest request = new NodeMoveRequest();
        {
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a3Node.getId());
            request.setDropPosition(1);


            //测试项目没有开启测试计划模块时能否使用
            testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
            this.requestPost(URL_POST_MODULE_MOVE, request).andExpect(status().is5xxServerError());
            //恢复
            testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, false);
        }
        //父节点内移动-移动到末位  在上面的基础上，a1挪到a2上面
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //父节点内移动-移动到中位 a1移动到a2-a3中间
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a1Node.getId(), a3Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //跨节点移动-移动到首位   a3移动到a1-b1前面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到末尾   a3移动到a1-a1后面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a1a1Node.getId(), a3Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到中位   a3移动到a1-b1和a1-a1中间，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-a3移动到首位pos小于2，是否触发计算函数 （先手动更改a1的pos为2，然后移动a3到a1前面）
        {
            //更改pos
            TestPlanModule updateModule = new TestPlanModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(2L);
            testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-移动到中位，前后节点pos差不大于2，是否触发计算函数（在上面的 a3-a1-a2的基础上， 先手动更改a1pos为3*64，a2的pos为3*64+2，然后移动a3到a1和a2中间）
        {
            //更改pos
            TestPlanModule updateModule = new TestPlanModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(3 * 64L);
            testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a2Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a3Node.getId(), a2Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到首位pos小于2，是否触发计算函数（先手动更改a1-b1的pos为2，然后移动a3到a1-b1前面，最后再移动回来）
        {
            //更改pos
            TestPlanModule updateModule = new TestPlanModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(2L);
            testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到中位，前后节点pos差不大于2，是否触发计算函数先手动更改a1-a1的pos为a1-b1+2，然后移动a3到a1-a1前面，最后再移动回来）
        {
            //更改pos
            TestPlanModule updateModule = new TestPlanModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(3 * 64L);
            testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a1a1Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //移动到没有子节点的节点下  a3移动到a2下
        {
            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(0);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            TestPlanModule a3Module = testPlanModuleMapper.selectByPrimaryKey(a3Node.getId());
            Assertions.assertEquals(a3Module.getParentId(), a2Node.getId());

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_POST_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        LOG_CHECK_LIST.add(
                new CheckLogModel(a1Node.getId(), OperationLogType.UPDATE, URL_POST_MODULE_MOVE)
        );
        LOG_CHECK_LIST.add(
                new CheckLogModel(a3Node.getId(), OperationLogType.UPDATE, URL_POST_MODULE_MOVE)
        );

        //判断权限
        this.requestPostPermissionTest(PermissionConstants.TEST_PLAN_MODULE_READ_UPDATE, URL_POST_MODULE_MOVE, request);
    }

    @Test
    @Order(101)
    public void deleteTestPlanTest() throws Exception {
        int allDataInDB = 999 + 40;
        if (StringUtils.isEmpty(groupTestPlanId7)) {
            this.testPlanAddTest();
        }

        //根据id删除 （删除 第61这1个)
        List<TestPlan> testPlanList = testPlanTestService.selectByProjectIdAndNames(project.getId(),
                new String[]{"testPlan_61"});

        this.requestGet(String.format(URL_GET_TEST_PLAN_DELETE, testPlanList.get(0).getId())).andExpect(status().isOk());
        allDataInDB--;
        Assertions.assertTrue(testPlanTestService.checkDataCount(project.getId(), allDataInDB));

        //根据id删除 （删除 第610-619这11个)
        testPlanList = testPlanTestService.selectByProjectIdAndNames(project.getId(),
                new String[]{"testPlan_61", "testPlan_610", "testPlan_611", "testPlan_612", "testPlan_613", "testPlan_614", "testPlan_615", "testPlan_616", "testPlan_617", "testPlan_618", "testPlan_619"});
        TestPlanBatchProcessRequest request = new TestPlanBatchProcessRequest();
        request.setProjectId(project.getId());
        request.setSelectIds(testPlanList.stream().map(TestPlan::getId).collect(Collectors.toList()));
        this.requestPostWithOk(URL_POST_TEST_PLAN_BATCH_DELETE, request);
        allDataInDB = allDataInDB - 10;
        Assertions.assertTrue(testPlanTestService.checkDataCount(project.getId(), allDataInDB));

        //删除组
        this.requestGetWithOk(String.format(URL_GET_TEST_PLAN_DELETE, groupTestPlanId7));
        allDataInDB--;
        Assertions.assertTrue(testPlanTestService.checkDataCount(project.getId(), allDataInDB));
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andGroupIdEqualTo(groupTestPlanId7);
        Assertions.assertEquals(testPlanMapper.countByExample(example), 0);


        //测试项目没有开启测试计划模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
        this.requestGet(String.format(URL_GET_TEST_PLAN_DELETE, testPlanList.get(0).getId())).andExpect(status().is5xxServerError());
        this.requestPost(URL_POST_TEST_PLAN_BATCH_DELETE, request).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        // 根据查询条件删除（ 删除plan_2)这一部分
        request = new TestPlanBatchProcessRequest();
        request.setProjectId(project.getId());
        request.setSelectAll(true);
        request.getCondition().setKeyword("plan_2");
        this.requestPostWithOk(URL_POST_TEST_PLAN_BATCH_DELETE, request);
        allDataInDB = allDataInDB - (1 + 10 + 100);
        Assertions.assertTrue(testPlanTestService.checkDataCount(project.getId(), allDataInDB));

        //批量删除的数据中包含group15这个用户组
        request = new TestPlanBatchProcessRequest();
        request.setSelectIds(Collections.singletonList(groupTestPlanId15));
        request.setProjectId(project.getId());
        this.requestPostWithOk(URL_POST_TEST_PLAN_BATCH_DELETE, request);
        allDataInDB--;
        Assertions.assertTrue(testPlanTestService.checkDataCount(project.getId(), allDataInDB));
        example.clear();
        example.createCriteria().andGroupIdEqualTo(groupTestPlanId15);
        Assertions.assertEquals(testPlanMapper.countByExample(example), 0);

        //根据a1a1Node模快删除
        BaseTreeNode a1a1Node = TestPlanTestUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-a1");
        request = new TestPlanBatchProcessRequest();
        request.setSelectAll(true);
        request.setModuleIds(Arrays.asList(a1a1Node.getId()));
        request.setProjectId(project.getId());
        this.requestPostWithOk(URL_POST_TEST_PLAN_BATCH_DELETE, request);
        allDataInDB = allDataInDB - 50;
        Assertions.assertTrue(testPlanTestService.checkDataCount(project.getId(), allDataInDB));

        //根据 a1b1Node模块以及planSty这个条件删除（应当删除0条，数据量不会变化）
        BaseTreeNode a1b1Node = TestPlanTestUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-b1");
        request = new TestPlanBatchProcessRequest();
        request.setSelectAll(true);
        request.setModuleIds(Arrays.asList(a1b1Node.getId()));
        request.getCondition().setKeyword("planSty");
        request.setProjectId(project.getId());
        this.requestPostWithOk(URL_POST_TEST_PLAN_BATCH_DELETE, request);
        Assertions.assertTrue(testPlanTestService.checkDataCount(project.getId(), allDataInDB));
    }

    @Test
    @Order(102)
    public void deleteModuleTest() throws Exception {
        this.preliminaryTree();

        // 删除没有文件的节点a1-b1-c1  检查是否级联删除根节点
        BaseTreeNode a1b1Node = TestPlanTestUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-b1");
        assert a1b1Node != null;

        //测试项目没有开启测试计划模块时能否使用
        testPlanTestService.removeProjectModule(project, PROJECT_MODULE, "testPlan");
        this.requestGet(String.format(URL_GET_MODULE_DELETE, a1b1Node.getId())).andExpect(status().is5xxServerError());
        //恢复
        testPlanTestService.resetProjectModule(project, PROJECT_MODULE);

        this.requestGetWithOk(String.format(URL_GET_MODULE_DELETE, a1b1Node.getId()));
        this.checkModuleIsEmpty(a1b1Node.getId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1b1Node.getId(), OperationLogType.DELETE, URL_GET_MODULE_DELETE)
        );

        // 删除有文件的节点 a1-a1      检查是否级联删除根节点
        BaseTreeNode a1a1Node = TestPlanTestUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-a1");
        assert a1a1Node != null;
        this.requestGetWithOk(String.format(URL_GET_MODULE_DELETE, a1a1Node.getId()));
        this.checkModuleIsEmpty(a1a1Node.getId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1a1Node.getId(), OperationLogType.DELETE, URL_GET_MODULE_DELETE)
        );

        //删除不存在的节点
        this.requestGet(String.format(URL_GET_MODULE_DELETE, IDGenerator.nextNum())).andExpect(status().is5xxServerError());
        // 测试删除根节点(根节点无法删除）
        this.requestGet(String.format(URL_GET_MODULE_DELETE, ModuleConstants.DEFAULT_NODE_ID)).andExpect(status().is5xxServerError());
        ;

        //service层判断：测试删除空集合
        testPlanModuleService.deleteModule(new ArrayList<>(), project.getId(), null, null, null);

        //service层判断：测试删除项目
        testPlanModuleService.deleteResources(project.getId());

        //判断权限
        this.requestGetPermissionTest(PermissionConstants.TEST_PLAN_MODULE_READ_DELETE, (String.format(URL_GET_MODULE_DELETE, IDGenerator.nextNum())));
    }

    private void checkModuleIsEmpty(String id) {
        TestPlanModuleExample example = new TestPlanModuleExample();
        example.createCriteria().andParentIdEqualTo(id);
        Assertions.assertEquals(testPlanModuleMapper.countByExample(example), 0);

        example = new TestPlanModuleExample();
        example.createCriteria().andIdEqualTo(id);
        Assertions.assertEquals(testPlanModuleMapper.countByExample(example), 0);

        //        该模块下已无测试计划
        Assertions.assertEquals(testPlanTestService.countByModuleId(id), 0);
    }


    public MvcResult responseFile(String url, MockMultipartFile file, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                        .file(file)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private List<BaseTreeNode> getFileModuleTreeNode() throws Exception {
        MvcResult result = this.requestGetWithOkAndReturn(String.format(URL_GET_MODULE_TREE, project.getId()));
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
    }


    private void preliminaryTree() throws Exception {
        if (CollectionUtils.isEmpty(preliminaryTreeNodes)) {
             /*
                这里需要获取修改过的树的结构。期望的最终结构是这样的（*为测试用例中挂载文件的节点， · 为空节点）：

                *默认节点
                |
                ·a1 +
                |   |
                |   ·a1-b1   +
                |   |        |
                |   |        ·a1-b1-c1
                |   |
                |   *a1-a1   +（创建的时候是a1，通过修改改为a1-a1）
                |   |        |
                |   |        ·a1-a1-c1(用于测试文件移动)
                |
                ·a2
                |
                ·a3
            */
            this.updateModuleTest();
        }
    }

    private void preliminaryTestPlan() throws Exception {
        if (ObjectUtils.anyNull(groupTestPlanId7, groupTestPlanId15)) {
            this.testPlanAddTest();
        }
    }

    private void checkModulePos(String firstNode, String secondNode, String thirdNode, boolean isRecalculate) {
        TestPlanModule firstModule = testPlanModuleMapper.selectByPrimaryKey(firstNode);
        TestPlanModule secondModule = testPlanModuleMapper.selectByPrimaryKey(secondNode);
        TestPlanModule thirdModule = null;
        Assertions.assertTrue(firstModule.getPos() < secondModule.getPos());
        if (StringUtils.isNotBlank(thirdNode)) {
            thirdModule = testPlanModuleMapper.selectByPrimaryKey(thirdNode);
            Assertions.assertTrue(secondModule.getPos() < thirdModule.getPos());
        }
        if (isRecalculate) {
            int limitPos = 64;
            Assertions.assertEquals(0, firstModule.getPos() % limitPos);
            Assertions.assertEquals(0, secondModule.getPos() % limitPos);
            if (thirdModule != null) {
                Assertions.assertEquals(0, thirdModule.getPos() % limitPos);
            }
        }
    }


    @Test
    @Order(110)
    public void testLog() throws Exception {
        Thread.sleep(5000);
        for (CheckLogModel checkLogModel : LOG_CHECK_LIST) {
            if (StringUtils.isEmpty(checkLogModel.getUrl())) {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType());
            } else {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType(), checkLogModel.getUrl());
            }
        }
    }


    private void checkModuleCount(Map<String, Object> moduleCountMap, long a1NodeCount, long a2NodeCount, long a3NodeCount, long a1a1NodeCount, long a1b1NodeCount) throws Exception {
        BaseTreeNode a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1");
        BaseTreeNode a2Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a2");
        BaseTreeNode a3Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a3");
        BaseTreeNode a1a1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        BaseTreeNode a1b1Node = TestPlanTestUtils.getNodeByName(preliminaryTreeNodes, "a1-b1");
        assert a1Node != null & a2Node != null & a3Node != null & a1a1Node != null & a1b1Node != null;
        //检查每个节点下的数据是否匹配的上（父节点的统计会一并添加上子节点的数据）
        moduleCountMap.forEach((k, v) -> {
            long value = Long.parseLong(String.valueOf(v));
            if (StringUtils.equals(k, a1Node.getId())) {
                Assertions.assertEquals(value, a1NodeCount + a1a1NodeCount + a1b1NodeCount);
            } else if (StringUtils.equals(k, a2Node.getId())) {
                Assertions.assertEquals(value, a2NodeCount);
            } else if (StringUtils.equals(k, a3Node.getId())) {
                Assertions.assertEquals(value, a3NodeCount);
            } else if (StringUtils.equals(k, a1a1Node.getId())) {
                Assertions.assertEquals(value, a1a1NodeCount);
            } else if (StringUtils.equals(k, a1b1Node.getId())) {
                Assertions.assertEquals(value, a1b1NodeCount);
            }
        });
    }

    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanManagementService testPlanManagementService;

    @Test
    @Order(111)
    public void serviceCheckTest() throws Exception {
        testPlanService.checkModule(ModuleConstants.DEFAULT_NODE_ID);

        //不存在的project测试
        boolean methodHasError = false;
        try {
            testPlanManagementService.checkModuleIsOpen(IDGenerator.nextStr(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, new ArrayList<>());
        } catch (Exception e) {
            methodHasError = true;
        }
        Assertions.assertTrue(methodHasError);

        //不存在的类型
        methodHasError = false;
        try {
            testPlanManagementService.checkModuleIsOpen(IDGenerator.nextStr(), IDGenerator.nextStr(), new ArrayList<>());
        } catch (Exception e) {
            methodHasError = true;
        }
        Assertions.assertTrue(methodHasError);
    }
}
