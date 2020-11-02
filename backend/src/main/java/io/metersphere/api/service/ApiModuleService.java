package io.metersphere.api.service;


import io.metersphere.api.dto.delimit.ApiModuleDTO;
import io.metersphere.api.dto.delimit.DragModuleRequest;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiModuleExample;
import io.metersphere.base.domain.TestCaseExample;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiModuleService {

    @Resource
    ApiModuleMapper apiModuleMapper;
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;

    public List<ApiModuleDTO> getNodeTreeByProjectId(String projectId) {
        ApiModuleExample testCaseNodeExample = new ApiModuleExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(projectId);
        testCaseNodeExample.setOrderByClause("create_time asc");
        List<ApiModule> nodes = apiModuleMapper.selectByExample(testCaseNodeExample);
        return getNodeTrees(nodes);
    }

    public List<ApiModuleDTO> getNodeTrees(List<ApiModule> nodes) {

        List<ApiModuleDTO> nodeTreeList = new ArrayList<>();
        Map<Integer, List<ApiModule>> nodeLevelMap = new HashMap<>();
        nodes.forEach(node -> {
            Integer level = node.getLevel();
            if (nodeLevelMap.containsKey(level)) {
                nodeLevelMap.get(level).add(node);
            } else {
                List<ApiModule> apiModules = new ArrayList<>();
                apiModules.add(node);
                nodeLevelMap.put(node.getLevel(), apiModules);
            }
        });
        List<ApiModule> rootNodes = Optional.ofNullable(nodeLevelMap.get(1)).orElse(new ArrayList<>());
        rootNodes.forEach(rootNode -> nodeTreeList.add(buildNodeTree(nodeLevelMap, rootNode)));
        return nodeTreeList;
    }

    /**
     * 递归构建节点树
     *
     * @param nodeLevelMap
     * @param rootNode
     * @return
     */
    private ApiModuleDTO buildNodeTree(Map<Integer, List<ApiModule>> nodeLevelMap, ApiModule rootNode) {

        ApiModuleDTO nodeTree = new ApiModuleDTO();
        BeanUtils.copyBean(nodeTree, rootNode);
        nodeTree.setLabel(rootNode.getName());

        List<ApiModule> lowerNodes = nodeLevelMap.get(rootNode.getLevel() + 1);
        if (lowerNodes == null) {
            return nodeTree;
        }
        List<ApiModuleDTO> children = Optional.ofNullable(nodeTree.getChildren()).orElse(new ArrayList<>());
        lowerNodes.forEach(node -> {
            if (node.getParentId() != null && node.getParentId().equals(rootNode.getId())) {
                children.add(buildNodeTree(nodeLevelMap, node));
                nodeTree.setChildren(children);
            }
        });

        return nodeTree;
    }


    public String addNode(ApiModule node) {
        validateNode(node);
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        apiModuleMapper.insertSelective(node);
        return node.getId();
    }

    private void validateNode(ApiModule node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            throw new RuntimeException(Translator.get("test_case_node_level_tip")
                    + TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
        }
        checkTestCaseNodeExist(node);
    }

    private void checkTestCaseNodeExist(ApiModule node) {
        if (node.getName() != null) {
            ApiModuleExample example = new ApiModuleExample();
            ApiModuleExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(node.getName())
                    .andProjectIdEqualTo(node.getProjectId());
            if (StringUtils.isNotBlank(node.getParentId())) {
                criteria.andParentIdEqualTo(node.getParentId());
            } else {
                criteria.andParentIdIsNull();
            }
            if (StringUtils.isNotBlank(node.getId())) {
                criteria.andIdNotEqualTo(node.getId());
            }
            if (apiModuleMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private List<TestCaseDTO> QueryTestCaseByNodeIds(List<String> nodeIds) {
        QueryTestCaseRequest testCaseRequest = new QueryTestCaseRequest();
        testCaseRequest.setNodeIds(nodeIds);
        return extTestCaseMapper.list(testCaseRequest);
    }

    public int editNode(DragModuleRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        checkTestCaseNodeExist(request);
        List<TestCaseDTO> apiModule = QueryTestCaseByNodeIds(request.getNodeIds());

        apiModule.forEach(testCase -> {
            StringBuilder path = new StringBuilder(testCase.getNodePath());
            List<String> pathLists = Arrays.asList(path.toString().split("/"));
            pathLists.set(request.getLevel(), request.getName());
            path.delete(0, path.length());
            for (int i = 1; i < pathLists.size(); i++) {
                path = path.append("/").append(pathLists.get(i));
            }
            testCase.setNodePath(path.toString());
        });

        batchUpdateTestCase(apiModule);

        return apiModuleMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andNodeIdIn(nodeIds);

        ApiModuleExample testCaseNodeExample = new ApiModuleExample();
        testCaseNodeExample.createCriteria().andIdIn(nodeIds);
        return apiModuleMapper.deleteByExample(testCaseNodeExample);
    }

    private void batchUpdateTestCase(List<TestCaseDTO> apiModule) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper testCaseMapper = sqlSession.getMapper(TestCaseMapper.class);
        apiModule.forEach((value) -> {
            testCaseMapper.updateByPrimaryKey(value);
        });
        sqlSession.flushStatements();
    }

    public void dragNode(DragModuleRequest request) {

        checkTestCaseNodeExist(request);

        List<String> nodeIds = request.getNodeIds();

        List<TestCaseDTO> apiModule = QueryTestCaseByNodeIds(nodeIds);

        ApiModuleDTO nodeTree = request.getNodeTree();

        List<ApiModule> updateNodes = new ArrayList<>();

        buildUpdateTestCase(nodeTree, apiModule, updateNodes, "/", "0", 1);

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateTestCaseNode(updateNodes);

        batchUpdateTestCase(apiModule);
    }

    private void buildUpdateTestCase(ApiModuleDTO rootNode, List<TestCaseDTO> apiModule,
                                     List<ApiModule> updateNodes, String rootPath, String pId, int level) {

        rootPath = rootPath + rootNode.getName();

        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }

        ApiModule testCaseNode = new ApiModule();
        testCaseNode.setId(rootNode.getId());
        testCaseNode.setLevel(level);
        testCaseNode.setParentId(pId);
        updateNodes.add(testCaseNode);

        for (TestCaseDTO item : apiModule) {
            if (StringUtils.equals(item.getNodeId(), rootNode.getId())) {
                item.setNodePath(rootPath);
            }
        }

        List<ApiModuleDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateTestCase(children.get(i), apiModule, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
            }
        }
    }

    private void batchUpdateTestCaseNode(List<ApiModule> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiModuleMapper apiModuleMapper = sqlSession.getMapper(ApiModuleMapper.class);
        updateNodes.forEach((value) -> {
            apiModuleMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
    }


}
