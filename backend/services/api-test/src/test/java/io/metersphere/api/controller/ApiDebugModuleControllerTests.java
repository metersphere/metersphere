package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiDebugRequest;
import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.debug.ModuleUpdateRequest;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.mapper.ApiDebugModuleMapper;
import io.metersphere.api.service.debug.ApiDebugModuleService;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDebugModuleControllerTests extends BaseTest {

    private static final String URL_MODULE_ADD = "/api/debug/module/add";
    private static final String URL_MODULE_UPDATE = "/api/debug/module/update";
    private static final String URL_MODULE_DELETE = "/api/debug/module/delete/%s";
    private static final String URL_MODULE_TREE = "/api/debug/module/tree";
    private static final String URL_MODULE_MOVE = "/api/debug/module/move";
    private static final String URL_FILE_MODULE_COUNT = "/api/debug/module/count";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static Project project;
    private static List<BaseTreeNode> preliminaryTreeNodes = new ArrayList<>();
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiDebugModuleMapper apiDebugModuleMapper;
    @Resource
    private ApiDebugMapper apiDebugMapper;
    @Resource
    private ApiDebugBlobMapper apiDebugBlobMapper;
    @Resource
    private ApiDebugModuleService apiDebugModuleService;

    public static BaseTreeNode getNodeByName(List<BaseTreeNode> preliminaryTreeNodes, String nodeName) {
        for (BaseTreeNode firstLevelNode : preliminaryTreeNodes) {
            if (StringUtils.equals(firstLevelNode.getName(), nodeName) && StringUtils.equals(firstLevelNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                return firstLevelNode;
            }
            if (CollectionUtils.isNotEmpty(firstLevelNode.getChildren())) {
                for (BaseTreeNode secondLevelNode : firstLevelNode.getChildren()) {
                    if (StringUtils.equals(secondLevelNode.getName(), nodeName) && StringUtils.equals(secondLevelNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                        return secondLevelNode;
                    }
                    if (CollectionUtils.isNotEmpty(secondLevelNode.getChildren())) {
                        for (BaseTreeNode thirdLevelNode : secondLevelNode.getChildren()) {
                            if (StringUtils.equals(thirdLevelNode.getName(), nodeName) && StringUtils.equals(thirdLevelNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                                return thirdLevelNode;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @BeforeEach
    public void initTestData() {
        if (project == null) {
            Project initProject = new Project();
            initProject.setId(IDGenerator.nextStr());
            initProject.setNum(null);
            initProject.setOrganizationId("100001");
            initProject.setName("接口调试的项目");
            initProject.setDescription("接口调试的项目");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            project = projectMapper.selectByPrimaryKey(initProject.getId());
        }
    }

    public void initApiDebugData(String moduleId) {
        ApiDebug apiDebug = new ApiDebug();
        apiDebug.setId(IDGenerator.nextStr());
        apiDebug.setProjectId(project.getId());
        apiDebug.setName(StringUtils.join("接口调试", apiDebug.getId()));
        apiDebug.setModuleId(moduleId);
        apiDebug.setProtocol(ApiConstants.HTTP_PROTOCOL);
        apiDebug.setMethod("GET");
        apiDebug.setPath(StringUtils.join("api/debug/", apiDebug.getId()));
        apiDebug.setCreateTime(System.currentTimeMillis());
        apiDebug.setUpdateTime(System.currentTimeMillis());
        apiDebug.setCreateUser("admin");
        apiDebug.setUpdateUser("admin");
        apiDebugMapper.insertSelective(apiDebug);
        ApiDebugBlob apiDebugBlob = new ApiDebugBlob();
        apiDebugBlob.setId(apiDebug.getId());
        apiDebugBlob.setRequest(new byte[0]);
        apiDebugBlob.setResponse(new byte[0]);
        apiDebugBlobMapper.insertSelective(apiDebugBlob);
    }

    public void initApiDebugTCPData(String moduleId) {
        ApiDebug apiDebug = new ApiDebug();
        apiDebug.setId(IDGenerator.nextStr());
        apiDebug.setProjectId(project.getId());
        apiDebug.setName(StringUtils.join("接口调试", apiDebug.getId()));
        apiDebug.setModuleId(moduleId);
        apiDebug.setProtocol("TCP");
        apiDebug.setCreateTime(System.currentTimeMillis());
        apiDebug.setUpdateTime(System.currentTimeMillis());
        apiDebug.setCreateUser("admin");
        apiDebug.setUpdateUser("admin");
        apiDebugMapper.insertSelective(apiDebug);
        ApiDebugBlob apiDebugBlob = new ApiDebugBlob();
        apiDebugBlob.setId(apiDebug.getId());
        apiDebugBlob.setRequest(new byte[0]);
        apiDebugBlob.setResponse(new byte[0]);
        apiDebugBlobMapper.insertSelective(apiDebugBlob);
    }

    @Test
    @Order(2)
    public void addModuleTestSuccess() throws Exception {
        //根目录下创建节点(a1）
        ModuleCreateRequest request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_MODULE_ADD, request);
        String returnId = mvcResult.getResponse().getContentAsString();
        Assertions.assertNotNull(returnId);
        List<BaseTreeNode> treeNodes = this.getDebugModuleTreeNode();
        BaseTreeNode a1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), request.getName())) {
                a1Node = baseTreeNode;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertNotNull(a1Node);
        initApiDebugData(a1Node.getId());
        initApiDebugTCPData(a1Node.getId());
        checkLog(a1Node.getId(), OperationLogType.ADD, URL_MODULE_ADD);

        //根目录下创建节点a2和a3，在a1下创建子节点a1-b1
        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a2");
        this.requestPostWithOkAndReturn(URL_MODULE_ADD, request);


        request.setName("a3");
        this.requestPostWithOkAndReturn(URL_MODULE_ADD, request);

        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_ADD, request);

        treeNodes = this.getDebugModuleTreeNode();
        BaseTreeNode a1b1Node = null;
        BaseTreeNode a2Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(childNode.getName(), "a1-b1") && StringUtils.equals(childNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                        a1b1Node = childNode;
                    }
                    Assertions.assertNotNull(childNode.getParentId());
                }
            } else if (StringUtils.equals(baseTreeNode.getName(), "a2") && StringUtils.equals(baseTreeNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                a2Node = baseTreeNode;
            }
        }
        Assertions.assertNotNull(a2Node);
        Assertions.assertNotNull(a1b1Node);
        initApiDebugData(a2Node.getId());
        initApiDebugTCPData(a2Node.getId());
        initApiDebugData(a1b1Node.getId());
        initApiDebugTCPData(a1b1Node.getId());
        checkLog(a2Node.getId(), OperationLogType.ADD, URL_MODULE_ADD);
        checkLog(a1b1Node.getId(), OperationLogType.ADD, URL_MODULE_ADD);

        //a1节点下可以继续添加a1节点
        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_ADD, request);

        //继续创建a1下继续创建a1-a1-b1,
        treeNodes = this.getDebugModuleTreeNode();
        BaseTreeNode a1ChildNode = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
                    Assertions.assertNotNull(childNode.getParentId());
                    if (StringUtils.equals(childNode.getName(), "a1") && StringUtils.equals(childNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                        a1ChildNode = childNode;
                    }
                }
            }
        }
        Assertions.assertNotNull(a1ChildNode);
        initApiDebugData(a1ChildNode.getId());
        checkLog(a1ChildNode.getId(), OperationLogType.ADD, URL_MODULE_ADD);

        //a1的子节点a1下继续创建节点a1-a1-c1
        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-a1-c1");
        request.setParentId(a1ChildNode.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_ADD, request);
        treeNodes = this.getDebugModuleTreeNode();
        BaseTreeNode a1a1c1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    Assertions.assertNotNull(secondNode.getParentId());
                    if (StringUtils.equals(secondNode.getName(), "a1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
                            Assertions.assertNotNull(thirdNode.getParentId());
                            if (StringUtils.equals(thirdNode.getName(), "a1-a1-c1") && StringUtils.equals(thirdNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                                a1a1c1Node = thirdNode;
                            }
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(a1a1c1Node);
        initApiDebugData(a1a1c1Node.getId());
        checkLog(a1a1c1Node.getId(), OperationLogType.ADD, URL_MODULE_ADD);

        //子节点a1-b1下继续创建节点a1-b1-c1
        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1-c1");
        request.setParentId(a1b1Node.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_ADD, request);
        treeNodes = this.getDebugModuleTreeNode();
        BaseTreeNode a1b1c1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(secondNode.getName(), "a1-b1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
                            if (StringUtils.equals(thirdNode.getName(), "a1-b1-c1") && StringUtils.equals(thirdNode.getType(), ModuleConstants.NODE_TYPE_DEFAULT)) {
                                a1b1c1Node = thirdNode;
                            }
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(a1b1c1Node);
        initApiDebugData(a1b1c1Node.getId());
        preliminaryTreeNodes = treeNodes;

        checkLog(a1b1c1Node.getId(), OperationLogType.ADD, URL_MODULE_ADD);
        //校验权限
        request = new ModuleCreateRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("defaultProject");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_ADD, URL_MODULE_ADD, request);

        /**
         测试能否正常做200个节点
         */
        String parentId = null;
        for (int i = 0; i < 210; i++) {
            ModuleCreateRequest perfRequest = new ModuleCreateRequest();
            perfRequest.setProjectId(project.getId());
            perfRequest.setName("500-test-root-" + i);
            if (StringUtils.isNotEmpty(parentId)) {
                perfRequest.setParentId(parentId);
            }
            if (i < 200) {
                MvcResult result = this.requestPostWithOkAndReturn(URL_MODULE_ADD, perfRequest);
                ResultHolder holder = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class);
                if (i % 50 == 0) {
                    //到20换下一层级
                    parentId = holder.getData().toString();
                }
            }
        }
        treeNodes = this.getDebugModuleTreeNode();
        preliminaryTreeNodes = treeNodes;
    }

    @Test
    @Order(3)
    public void addModuleTestError() throws Exception {
        this.preliminaryData();

        BaseTreeNode a1Node = getNodeByName(preliminaryTreeNodes, "a1");
        assert a1Node != null;

        //参数校验
        ModuleCreateRequest request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        this.requestPost(URL_MODULE_ADD, request).andExpect(BAD_REQUEST_MATCHER);
        request = new ModuleCreateRequest();
        request.setName("none");
        this.requestPost(URL_MODULE_ADD, request).andExpect(BAD_REQUEST_MATCHER);
        request = new ModuleCreateRequest();
        this.requestPost(URL_MODULE_ADD, request).andExpect(BAD_REQUEST_MATCHER);
        request = new ModuleCreateRequest();
        request.setParentId(null);
        this.requestPost(URL_MODULE_ADD, request).andExpect(BAD_REQUEST_MATCHER);
        //名称存在特殊字符
        request.setName("a1/a2");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_MODULE_ADD, request).andExpect(BAD_REQUEST_MATCHER);

        //父节点ID不存在的
        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("ParentIsUUID");
        request.setParentId(IDGenerator.nextStr());
        this.requestPost(URL_MODULE_ADD, request).andExpect(ERROR_REQUEST_MATCHER);

        //添加重复的a1节点
        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        this.requestPost(URL_MODULE_ADD, request).andExpect(ERROR_REQUEST_MATCHER);

        //a1节点下添加重复的a1-b1节点
        request = new ModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_MODULE_ADD, request).andExpect(ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(4)
    public void updateModuleTestSuccess() throws Exception {
        if (CollectionUtils.isEmpty(preliminaryTreeNodes)) {
            this.addModuleTestSuccess();
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
        ModuleUpdateRequest updateRequest = new ModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-a1");
        //无法拿到当前用户的projectID 所以需要传入
        mockMvc.perform(MockMvcRequestBuilders.post(URL_MODULE_UPDATE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, project.getId())
                        .content(JSON.toJSONString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        preliminaryTreeNodes = this.getDebugModuleTreeNode();
        checkLog(a1Node.getId(), OperationLogType.UPDATE, URL_MODULE_UPDATE);

        //校验权限
        ApiDebugModuleExample example = new ApiDebugModuleExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("defaultProject");
        List<ApiDebugModule> apiDebugModules = apiDebugModuleMapper.selectByExample(example);
        assert CollectionUtils.isNotEmpty(apiDebugModules);
        updateRequest = new ModuleUpdateRequest();
        updateRequest.setId(apiDebugModules.getFirst().getId());
        updateRequest.setName("default-update-Project");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_UPDATE, URL_MODULE_UPDATE, updateRequest);
    }

    @Test
    @Order(5)
    public void updateModuleTestError() throws Exception {
        BaseTreeNode a1Node = getNodeByName(preliminaryTreeNodes, "a1-a1");
        assert a1Node != null;
        //反例-参数校验
        ModuleUpdateRequest updateRequest = new ModuleUpdateRequest();
        this.requestPost(URL_MODULE_UPDATE, updateRequest).andExpect(BAD_REQUEST_MATCHER);
        //参数名称带有特殊字符
        updateRequest = new ModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1/a2");
        this.requestPost(URL_MODULE_UPDATE, updateRequest).andExpect(BAD_REQUEST_MATCHER);

        //id不存在
        updateRequest = new ModuleUpdateRequest();
        updateRequest.setId(IDGenerator.nextStr());
        updateRequest.setName(IDGenerator.nextStr());
        this.requestPost(URL_MODULE_UPDATE, updateRequest).andExpect(ERROR_REQUEST_MATCHER);

        //名称重复   a1-a1改为a1-b1
        updateRequest = new ModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-b1");
        this.requestPost(URL_MODULE_UPDATE, updateRequest).andExpect(ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(6)
    public void moveTest() throws Exception {
        this.preliminaryData();
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
        BaseTreeNode a1Node = getNodeByName(preliminaryTreeNodes, "a1");
        BaseTreeNode a2Node = getNodeByName(preliminaryTreeNodes, "a2");
        BaseTreeNode a3Node = getNodeByName(preliminaryTreeNodes, "a3");
        BaseTreeNode a1a1Node = getNodeByName(preliminaryTreeNodes, "a1-a1");
        BaseTreeNode a1b1Node = getNodeByName(preliminaryTreeNodes, "a1-b1");

        //父节点内移动-移动到首位 a1挪到a3后面
        NodeMoveRequest request = new NodeMoveRequest();
        {
            assert a1Node != null;
            request.setDragNodeId(a1Node.getId());
            assert a3Node != null;
            request.setDropNodeId(a3Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, false);
        }
        //父节点内移动-移动到末位  在上面的基础上，a1挪到a2上面
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            assert a2Node != null;
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //父节点内移动-移动到中位 a1移动到a2-a3中间
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a1Node.getId(), a3Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //跨节点移动-移动到首位   a3移动到a1-b1前面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到末尾   a3移动到a1-a1后面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            assert a1a1Node != null;
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a1a1Node.getId(), a3Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到中位   a3移动到a1-b1和a1-a1中间，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-a3移动到首位pos小于2，是否触发计算函数 （先手动更改a1的pos为2，然后移动a3到a1前面）
        {
            //更改pos
            ApiDebugModule updateModule = new ApiDebugModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(2L);
            apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-移动到中位，前后节点pos差不大于2，是否触发计算函数（在上面的 a3-a1-a2的基础上， 先手动更改a1pos为3*64，a2的pos为3*64+2，然后移动a3到a1和a2中间）
        {
            //更改pos
            ApiDebugModule updateModule = new ApiDebugModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(3 * 64L);
            apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a2Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a3Node.getId(), a2Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到首位pos小于2，是否触发计算函数（先手动更改a1-b1的pos为2，然后移动a3到a1-b1前面，最后再移动回来）
        {
            //更改pos
            ApiDebugModule updateModule = new ApiDebugModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(2L);
            apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到中位，前后节点pos差不大于2，是否触发计算函数先手动更改a1-a1的pos为a1-b1+2，然后移动a3到a1-a1前面，最后再移动回来）
        {
            //更改pos
            ApiDebugModule updateModule = new ApiDebugModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(3 * 64L);
            apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a1a1Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //移动到没有子节点的节点下  a3移动到a2下
        {
            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(0);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            ApiDebugModule a3Module = apiDebugModuleMapper.selectByPrimaryKey(a3Node.getId());
            Assertions.assertEquals(a3Module.getParentId(), a2Node.getId());

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        checkLog(a1Node.getId(), OperationLogType.UPDATE, URL_MODULE_MOVE);
        checkLog(a3Node.getId(), OperationLogType.UPDATE, URL_MODULE_MOVE);
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_UPDATE, URL_MODULE_MOVE, request);
    }

    @Test
    @Order(7)
    public void moveTestError() throws Exception {
        this.preliminaryData();
        BaseTreeNode a1Node = getNodeByName(preliminaryTreeNodes, "a1");
        BaseTreeNode a2Node = getNodeByName(preliminaryTreeNodes, "a2");
        //drag节点为空
        NodeMoveRequest request = new NodeMoveRequest();
        request.setDragNodeId(null);
        assert a1Node != null;
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_MOVE, request).andExpect(BAD_REQUEST_MATCHER);
        //drag节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(IDGenerator.nextStr());
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_MOVE, request).andExpect(ERROR_REQUEST_MATCHER);

        //drop节点为空
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(null);
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_MOVE, request).andExpect(BAD_REQUEST_MATCHER);

        //drop节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(IDGenerator.nextStr());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_MOVE, request).andExpect(ERROR_REQUEST_MATCHER);

        //position为0的时候节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(IDGenerator.nextStr());
        request.setDropPosition(0);
        this.requestPost(URL_MODULE_MOVE, request).andExpect(ERROR_REQUEST_MATCHER);

        //dragNode和dropNode一样
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_MOVE, request).andExpect(ERROR_REQUEST_MATCHER);

        //position不是-1 0 1
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        assert a2Node != null;
        request.setDropNodeId(a2Node.getId());
        request.setDropPosition(4);
        this.requestPost(URL_MODULE_MOVE, request).andExpect(ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(8)
    public void TestModuleCountSuccess() throws Exception {
        this.preliminaryData();
        ApiDebugRequest request = new ApiDebugRequest();
        MvcResult moduleCountMvcResult = this.requestPostWithOkAndReturn(URL_FILE_MODULE_COUNT, request);
        Map<String, Integer> moduleCountResult = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Map.class);
        Assertions.assertTrue(moduleCountResult.containsKey("all"));
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_READ, URL_FILE_MODULE_COUNT, request);

    }

    @Test
    @Order(10)
    public void deleteModuleTestSuccess() throws Exception {
        this.preliminaryData();

        // 删除没有文件的节点a1-b1-c1  检查是否级联删除根节点
        BaseTreeNode a1b1Node = getNodeByName(this.getDebugModuleTreeNode(), "a1-b1");
        assert a1b1Node != null;
        this.requestGetWithOk(String.format(URL_MODULE_DELETE, a1b1Node.getId()));
        this.checkModuleIsEmpty(a1b1Node.getId());
        checkLog(a1b1Node.getId(), OperationLogType.DELETE, URL_MODULE_DELETE);

        // 删除有文件的节点 a1-a1      检查是否级联删除根节点
        BaseTreeNode a1a1Node = getNodeByName(this.getDebugModuleTreeNode(), "a1-a1");
        assert a1a1Node != null;
        this.requestGetWithOk(String.format(URL_MODULE_DELETE, a1a1Node.getId()));
        this.checkModuleIsEmpty(a1a1Node.getId());
        checkLog(a1a1Node.getId(), OperationLogType.DELETE, URL_MODULE_DELETE);

        //删除不存在的节点
        this.requestGet(String.format(URL_MODULE_DELETE, IDGenerator.nextNum())).andExpect(ERROR_REQUEST_MATCHER);
        // 测试删除根节点
        this.requestGet(String.format(URL_MODULE_DELETE, ModuleConstants.DEFAULT_NODE_ID)).andExpect(ERROR_REQUEST_MATCHER);

        //service层判断：测试删除空集合
        apiDebugModuleService.deleteModule(new ArrayList<>(), "admin", DEFAULT_PROJECT_ID);
        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_DELETE, String.format(URL_MODULE_DELETE, IDGenerator.nextNum()));

    }

    private List<BaseTreeNode> getDebugModuleTreeNode() throws Exception {
        MvcResult result = this.requestGetWithOkAndReturn(URL_MODULE_TREE);
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        this.requestGetWithOkAndReturn(URL_MODULE_TREE);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
    }

    private void preliminaryData() throws Exception {
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
                |   |        ·a1-a1-c1
                |
                ·a2
                |
                ·a3
            */
            this.updateModuleTestSuccess();
        }
    }

    private void checkModuleIsEmpty(String id) {
        ApiDebugModuleExample example = new ApiDebugModuleExample();
        example.createCriteria().andParentIdEqualTo(id);
        Assertions.assertEquals(0, apiDebugModuleMapper.countByExample(example));

        ApiDebugExample apiDebugExample = new ApiDebugExample();
        example = new ApiDebugModuleExample();
        example.createCriteria().andIdEqualTo(id);
        Assertions.assertEquals(0, apiDebugModuleMapper.countByExample(example));
        apiDebugExample.createCriteria().andModuleIdEqualTo(id);
        Assertions.assertEquals(0, apiDebugMapper.countByExample(apiDebugExample));
    }

    private void checkModulePos(String firstNode, String secondNode, String thirdNode, boolean isRecalculate) {
        ApiDebugModule firstModule = apiDebugModuleMapper.selectByPrimaryKey(firstNode);
        ApiDebugModule secondModule = apiDebugModuleMapper.selectByPrimaryKey(secondNode);
        ApiDebugModule thirdModule = null;
        Assertions.assertTrue(firstModule.getPos() < secondModule.getPos());
        if (StringUtils.isNotBlank(thirdNode)) {
            thirdModule = apiDebugModuleMapper.selectByPrimaryKey(thirdNode);
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


}
