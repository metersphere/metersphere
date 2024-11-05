package io.metersphere.functional.controller;

import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.functional.domain.FunctionalCaseModuleExample;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseModuleMapper;
import io.metersphere.functional.request.FunctionalCaseModuleCreateRequest;
import io.metersphere.functional.request.FunctionalCaseModuleUpdateRequest;
import io.metersphere.functional.service.FunctionalCaseModuleService;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseModuleControllerTests extends BaseTest {

    private static Project project;
    private static final String URL_MODULE_TREE = "/functional/case/module/tree/";
    private static final String URL_MODULE_TREE_ADD = "/functional/case/module/add";
    private static final String URL_MODULE_TREE_UPDATE = "/functional/case/module/update";

    private static List<BaseTreeNode> preliminaryTreeNodes = new ArrayList<>();

    private static final String URL_MODULE_TREE_MOVE = "/functional/case/module/move";
    private static final String URL_MODULE_TREE_DELETE = "/functional/case/module/delete/";
    private static final String URL_MODULE_TREE_TRASH = "/functional/case/module/trash/tree/";


    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;
    @Resource
    private FunctionalCaseModuleMapper functionalCaseModuleMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private ProjectMapper projectMapper;

    @BeforeEach
    public void initTestData() {
        if (project == null) {
            Project initProject = new Project();
            initProject.setId(IDGenerator.nextStr());
            initProject.setNum(null);
            initProject.setOrganizationId("100001");
            initProject.setName("caseModuleTestProject");
            initProject.setDescription("caseModuleTestProject");
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

    @Test
    @Order(1)
    public void emptyDataTest() throws Exception {
        //空数据下，检查模块树
        List<BaseTreeNode> treeNodes = this.getFunctionalCaseModuleTreeNode();
        //检查有没有默认节点
        boolean hasNode = false;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getId(), ModuleConstants.DEFAULT_NODE_ID)) {
                hasNode = true;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertTrue(hasNode);
    }

    @Test
    @Order(2)
    public void addModuleTestSuccess() throws Exception {
        //根目录下创建节点(a1）
        FunctionalCaseModuleCreateRequest request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);
        String returnId = mvcResult.getResponse().getContentAsString();
        Assertions.assertNotNull(returnId);
        List<BaseTreeNode> treeNodes = this.getFunctionalCaseModuleTreeNode();
        BaseTreeNode a1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), request.getName())) {
                a1Node = baseTreeNode;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertNotNull(a1Node);

        //根目录下创建节点a2和a3，在a1下创建子节点a1-b1
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a2");
        this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);

        request.setName("a3");
        this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);

        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);

        treeNodes = this.getFunctionalCaseModuleTreeNode();
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

        //a1节点下可以继续添加a1节点
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);

        //继续创建a1下继续创建a1-a1-b1,
        treeNodes = this.getFunctionalCaseModuleTreeNode();
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

        //a1的子节点a1下继续创建节点a1-a1-c1
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-a1-c1");
        request.setParentId(a1ChildNode.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);
        treeNodes = this.getFunctionalCaseModuleTreeNode();
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

        //子节点a1-b1下继续创建节点a1-b1-c1
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1-c1");
        request.setParentId(a1b1Node.getId());
        this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);
        treeNodes = this.getFunctionalCaseModuleTreeNode();
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


        /**
         测试能否正常做200个节点
         */
        String parentId = null;
        for (int i = 0; i < 210; i++) {
            FunctionalCaseModuleCreateRequest perfRequest = new FunctionalCaseModuleCreateRequest();
            perfRequest.setProjectId(project.getId());
            perfRequest.setName("500-test-root-" + i);
            if (StringUtils.isNotEmpty(parentId)) {
                perfRequest.setParentId(parentId);
            }
            if (i < 200) {
                MvcResult result = this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, perfRequest);
                ResultHolder holder = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class);
                if (i % 50 == 0) {
                    //到20换下一层级
                    parentId = holder.getData().toString();
                }
            }
        }
        treeNodes = this.getFunctionalCaseModuleTreeNode();
        preliminaryTreeNodes = treeNodes;
    }

    @Test
    @Order(3)
    public void addModuleTestError() throws Exception {
        this.preliminaryData();

        BaseTreeNode a1Node = getNodeByName(preliminaryTreeNodes, "a1");
        assert a1Node != null;

        //参数校验
        FunctionalCaseModuleCreateRequest request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().isBadRequest());
        request = new FunctionalCaseModuleCreateRequest();
        request.setName("none");
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().isBadRequest());
        request = new FunctionalCaseModuleCreateRequest();
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().isBadRequest());
        request = new FunctionalCaseModuleCreateRequest();
        request.setParentId(null);
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().isBadRequest());

        //父节点ID不存在的
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("ParentIsUUID");
        request.setParentId(IDGenerator.nextStr());
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().is5xxServerError());

        //添加重复的a1节点
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().is5xxServerError());

        //a1节点下添加重复的a1-b1节点
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().is5xxServerError());

        //子节点的项目ID和父节点的不匹配
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(IDGenerator.nextStr());
        request.setName("RandomUUID");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().is5xxServerError());

        //项目ID和父节点的不匹配
        request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId("100001100001");
        request.setName("RandomUUID");
        request.setParentId(a1Node.getId());
        this.requestPost(URL_MODULE_TREE_ADD, request).andExpect(status().is5xxServerError());
    }

    public static BaseTreeNode getNodeByName(List<BaseTreeNode> preliminaryTreeNodes, String nodeName) {
        for (BaseTreeNode firstLevelNode : preliminaryTreeNodes) {
            if (StringUtils.equals(firstLevelNode.getName(), nodeName)) {
                return firstLevelNode;
            }
            if (CollectionUtils.isNotEmpty(firstLevelNode.getChildren())) {
                for (BaseTreeNode secondLevelNode : firstLevelNode.getChildren()) {
                    if (StringUtils.equals(secondLevelNode.getName(), nodeName)) {
                        return secondLevelNode;
                    }
                    if (CollectionUtils.isNotEmpty(secondLevelNode.getChildren())) {
                        for (BaseTreeNode thirdLevelNode : secondLevelNode.getChildren()) {
                            if (StringUtils.equals(thirdLevelNode.getName(), nodeName)) {
                                return thirdLevelNode;
                            }
                        }
                    }
                }
            }
        }
        return null;
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
        FunctionalCaseModuleUpdateRequest updateRequest = new FunctionalCaseModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-a1");
        this.requestPostWithOkAndReturn(URL_MODULE_TREE_UPDATE, updateRequest);

        preliminaryTreeNodes = this.getFunctionalCaseModuleTreeNode();
    }

    @Test
    @Order(5)
    public void updateModuleTestError() throws Exception {
        BaseTreeNode a1Node = getNodeByName(preliminaryTreeNodes, "a1-a1");
        assert a1Node != null;
        //反例-参数校验
        FunctionalCaseModuleUpdateRequest updateRequest = new FunctionalCaseModuleUpdateRequest();
        this.requestPost(URL_MODULE_TREE_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //id不存在
        updateRequest = new FunctionalCaseModuleUpdateRequest();
        updateRequest.setId(IDGenerator.nextStr());
        updateRequest.setName(IDGenerator.nextStr());
        this.requestPost(URL_MODULE_TREE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

        //名称重复   a1-a1改为a1-b1
        updateRequest = new FunctionalCaseModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-b1");
        this.requestPost(URL_MODULE_TREE_UPDATE, updateRequest).andExpect(status().is5xxServerError());
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
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a3Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, false);
        }
        //父节点内移动-移动到末位  在上面的基础上，a1挪到a2上面
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //父节点内移动-移动到中位 a1移动到a2-a3中间
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a1Node.getId(), a3Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //跨节点移动-移动到首位   a3移动到a1-b1前面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到末尾   a3移动到a1-a1后面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a1a1Node.getId(), a3Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到中位   a3移动到a1-b1和a1-a1中间，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-a3移动到首位pos小于2，是否触发计算函数 （先手动更改a1的pos为2，然后移动a3到a1前面）
        {
            //更改pos
            FunctionalCaseModule updateModule = new FunctionalCaseModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(2L);
            functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-移动到中位，前后节点pos差不大于2，是否触发计算函数（在上面的 a3-a1-a2的基础上， 先手动更改a1pos为3*64，a2的pos为3*64+2，然后移动a3到a1和a2中间）
        {
            //更改pos
            FunctionalCaseModule updateModule = new FunctionalCaseModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(3 * 64L);
            functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a2Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a3Node.getId(), a2Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到首位pos小于2，是否触发计算函数（先手动更改a1-b1的pos为2，然后移动a3到a1-b1前面，最后再移动回来）
        {
            //更改pos
            FunctionalCaseModule updateModule = new FunctionalCaseModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(2L);
            functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到中位，前后节点pos差不大于2，是否触发计算函数先手动更改a1-a1的pos为a1-b1+2，然后移动a3到a1-a1前面，最后再移动回来）
        {
            //更改pos
            FunctionalCaseModule updateModule = new FunctionalCaseModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(3 * 64L);
            functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a1a1Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //移动到没有子节点的节点下  a3移动到a2下
        {
            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(0);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            FunctionalCaseModule a3Module = functionalCaseModuleMapper.selectByPrimaryKey(a3Node.getId());
            Assertions.assertEquals(a3Module.getParentId(), a2Node.getId());

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(URL_MODULE_TREE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

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
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_TREE_MOVE, request).andExpect(status().isBadRequest());
        //drag节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(IDGenerator.nextStr());
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_TREE_MOVE, request).andExpect(status().is5xxServerError());

        //drop节点为空
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(null);
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_TREE_MOVE, request).andExpect(status().isBadRequest());

        //drop节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(IDGenerator.nextStr());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_TREE_MOVE, request).andExpect(status().is5xxServerError());

        //position为0的时候节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(IDGenerator.nextStr());
        request.setDropPosition(0);
        this.requestPost(URL_MODULE_TREE_MOVE, request).andExpect(status().is5xxServerError());

        //dragNode和dropNode一样
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(URL_MODULE_TREE_MOVE, request).andExpect(status().is5xxServerError());

        //position不是-1 0 1
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(a2Node.getId());
        request.setDropPosition(4);
        this.requestPost(URL_MODULE_TREE_MOVE, request).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(8)
    public void deleteModuleTestSuccess() throws Exception {
        this.preliminaryData();

        // 删除没有用例的节点a1-b1-c1  检查是否级联删除根节点
        BaseTreeNode a1b1Node = getNodeByName(this.getFunctionalCaseModuleTreeNode(), "a1-b1");
        assert a1b1Node != null;
        this.requestGetWithOk(URL_MODULE_TREE_DELETE + a1b1Node.getId());
        this.checkModuleIsEmpty(a1b1Node.getId());

        // 删除有用例的节点 a1-a1      检查是否级联删除根节点
        //创建数据
        BaseTreeNode a1a1Node = getNodeByName(this.getFunctionalCaseModuleTreeNode(), "a1-a1");
        FunctionalCase name = createCase(a1a1Node, false, "name");
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(name.getId());
        Assertions.assertNotNull(functionalCase);
        this.requestGetWithOk(URL_MODULE_TREE_DELETE + a1a1Node.getId());
        this.checkModuleIsEmpty(a1a1Node.getId());
        FunctionalCase functionalCaseDel = functionalCaseMapper.selectByPrimaryKey(name.getId());
        Assertions.assertTrue(functionalCaseDel.getDeleted());
        Assertions.assertTrue(StringUtils.equals(functionalCaseDel.getModuleId(), "root"));

        //删除不存在的节点
        this.requestGetWithOk(URL_MODULE_TREE_DELETE + IDGenerator.nextNum());
        // 测试删除根节点
        this.requestGetWithOk(URL_MODULE_TREE_DELETE + ModuleConstants.DEFAULT_NODE_ID);

        //service层判断：测试删除空集合
        functionalCaseModuleService.deleteModuleByIds(new ArrayList<>(), new ArrayList<>(), "admin");


    }


    @Test
    @Order(9)
    public void getTrashTreeSuccess() throws Exception {
        //回收站为空
        MvcResult mvcResult = this.requestGetAndReturn(URL_MODULE_TREE_TRASH + project.getId());
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<BaseTreeNode> baseTreeNodes = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
        Assertions.assertEquals(1, baseTreeNodes.size());
        //回收站有数据
        FunctionalCaseModuleCreateRequest request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("gyq");
        MvcResult mvcResultAdd = this.requestPostWithOkAndReturn(URL_MODULE_TREE_ADD, request);
        String returnId = mvcResultAdd.getResponse().getContentAsString();
        Assertions.assertNotNull(returnId);
        List<BaseTreeNode> treeNodes = this.getFunctionalCaseModuleTreeNode();
        BaseTreeNode a1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), request.getName())) {
                a1Node = baseTreeNode;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertNotNull(a1Node);
        createCase(a1Node, true, "name1");
        MvcResult mvcResultTrash = this.requestGetAndReturn(URL_MODULE_TREE_TRASH + project.getId());
        String contentTrash = mvcResultTrash.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolderTrash = JSON.parseObject(contentTrash, ResultHolder.class);
        List<BaseTreeNode> baseTreeNodeTrashList = JSON.parseArray(JSON.toJSONString(resultHolderTrash.getData()), BaseTreeNode.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(baseTreeNodeTrashList));
        // 没case
        mvcResultTrash = this.requestGetAndReturn(URL_MODULE_TREE_TRASH + "projectIdX");
        contentTrash = mvcResultTrash.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolderTrash = JSON.parseObject(contentTrash, ResultHolder.class);
        baseTreeNodeTrashList = JSON.parseArray(JSON.toJSONString(resultHolderTrash.getData()), BaseTreeNode.class);
        Assertions.assertTrue(CollectionUtils.isEmpty(baseTreeNodeTrashList));
    }

    private FunctionalCase createCase(BaseTreeNode a1a1Node, Boolean deleted, String name) {
        FunctionalCase functionalCase = new FunctionalCase();
        functionalCase.setName(name);
        functionalCase.setNum(100001l);
        functionalCase.setModuleId(a1a1Node.getId());
        functionalCase.setProjectId(project.getId());
        functionalCase.setDeleted(deleted);
        functionalCase.setTemplateId("default_template");
        functionalCase.setId(IDGenerator.nextStr());
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setCaseEditType("Text");
        functionalCase.setPos(500L);
        functionalCase.setVersionId("12335");
        functionalCase.setRefId(functionalCase.getId());
        functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
        functionalCase.setPublicCase(false);
        functionalCase.setLatest(true);
        functionalCase.setCreateUser("gyq");
        functionalCase.setCreateTime(System.currentTimeMillis());
        functionalCase.setUpdateUser("gyq");
        functionalCase.setUpdateTime(System.currentTimeMillis());
        functionalCaseMapper.insertSelective(functionalCase);
        return functionalCase;
    }

    private void checkModuleIsEmpty(String id) {
        FunctionalCaseModuleExample example = new FunctionalCaseModuleExample();
        example.createCriteria().andParentIdEqualTo(id);
        Assertions.assertEquals(functionalCaseModuleMapper.countByExample(example), 0);
    }

    private List<BaseTreeNode> getFunctionalCaseModuleTreeNode() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL_MODULE_TREE + "/" + project.getId()).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, project.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
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
                |   |        ·a1-a1-c1(用于测试文件移动)
                |
                ·a2
                |
                ·a3
            */
            this.updateModuleTestSuccess();
        }
    }

    private void checkModulePos(String firstNode, String secondNode, String thirdNode, boolean isRecalculate) {
        FunctionalCaseModule firstModule = functionalCaseModuleMapper.selectByPrimaryKey(firstNode);
        FunctionalCaseModule secondModule = functionalCaseModuleMapper.selectByPrimaryKey(secondNode);
        FunctionalCaseModule thirdModule = null;
        Assertions.assertTrue(firstModule.getPos() < secondModule.getPos());
        if (StringUtils.isNotBlank(thirdNode)) {
            thirdModule = functionalCaseModuleMapper.selectByPrimaryKey(thirdNode);
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
