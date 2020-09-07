package io.metersphere.track.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.exception.ExcelException;
import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.request.testcase.DragNodeRequest;
import io.metersphere.track.request.testcase.QueryNodeRequest;
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
public class TestCaseNodeService {

    @Resource
    TestCaseNodeMapper testCaseNodeMapper;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    ProjectMapper projectMapper;

    public String addNode(TestCaseNode node) {
        validateNode(node);
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        node.setId(UUID.randomUUID().toString());
        testCaseNodeMapper.insertSelective(node);
        return node.getId();
    }

    private void validateNode(TestCaseNode node) {
        if (node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH) {
            throw new RuntimeException(Translator.get("test_case_node_level_tip")
                    + TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
        }
        checkTestCaseNodeExist(node);
    }

    private void checkTestCaseNodeExist(TestCaseNode node) {
        if (node.getName() != null) {
            TestCaseNodeExample example = new TestCaseNodeExample();
            TestCaseNodeExample.Criteria criteria = example.createCriteria();
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
            if (testCaseNodeMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    public List<TestCaseNodeDTO> getNodeTreeByProjectId(String projectId) {
        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(projectId);
        testCaseNodeExample.setOrderByClause("create_time asc");
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);
        return getNodeTrees(nodes);
    }

    public List<TestCaseNodeDTO> getNodeTrees(List<TestCaseNode> nodes) {

        List<TestCaseNodeDTO> nodeTreeList = new ArrayList<>();

        Map<Integer, List<TestCaseNode>> nodeLevelMap = new HashMap<>();

        nodes.forEach(node -> {
            Integer level = node.getLevel();
            if (nodeLevelMap.containsKey(level)) {
                nodeLevelMap.get(level).add(node);
            } else {
                List<TestCaseNode> testCaseNodes = new ArrayList<>();
                testCaseNodes.add(node);
                nodeLevelMap.put(node.getLevel(), testCaseNodes);
            }
        });

        List<TestCaseNode> rootNodes = Optional.ofNullable(nodeLevelMap.get(1)).orElse(new ArrayList<>());
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
    private TestCaseNodeDTO buildNodeTree(Map<Integer, List<TestCaseNode>> nodeLevelMap, TestCaseNode rootNode) {

        TestCaseNodeDTO nodeTree = new TestCaseNodeDTO();
        BeanUtils.copyBean(nodeTree, rootNode);
        nodeTree.setLabel(rootNode.getName());

        List<TestCaseNode> lowerNodes = nodeLevelMap.get(rootNode.getLevel() + 1);
        if (lowerNodes == null) {
            return nodeTree;
        }

        List<TestCaseNodeDTO> children = Optional.ofNullable(nodeTree.getChildren()).orElse(new ArrayList<>());

        lowerNodes.forEach(node -> {
            if (node.getParentId() != null && node.getParentId().equals(rootNode.getId())) {
                children.add(buildNodeTree(nodeLevelMap, node));
                nodeTree.setChildren(children);
            }
        });

        return nodeTree;
    }

    public int editNode(DragNodeRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        checkTestCaseNodeExist(request);
        List<TestCaseDTO> testCases = QueryTestCaseByNodeIds(request.getNodeIds());

        testCases.forEach(testCase -> {
            StringBuilder path = new StringBuilder(testCase.getNodePath());
            List<String> pathLists = Arrays.asList(path.toString().split("/"));
            pathLists.set(request.getLevel(), request.getName());
            path.delete(0, path.length());
            for (int i = 1; i < pathLists.size(); i++) {
                path = path.append("/").append(pathLists.get(i));
            }
            testCase.setNodePath(path.toString());
        });

        batchUpdateTestCase(testCases);

        return testCaseNodeMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andNodeIdIn(nodeIds);
        testCaseMapper.deleteByExample(testCaseExample);

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andIdIn(nodeIds);
        return testCaseNodeMapper.deleteByExample(testCaseNodeExample);
    }

    /**
     * 获取当前计划下
     * 有关联数据的节点
     *
     * @param planId
     * @return
     */
    public List<TestCaseNodeDTO> getNodeByPlanId(String planId) {

        List<TestCaseNodeDTO> list = new ArrayList<>();
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        projectIds.forEach(id -> {
            String name = projectMapper.selectByPrimaryKey(id).getName();
            List<TestCaseNodeDTO> nodeList = getNodeDTO(id, planId);
            TestCaseNodeDTO testCaseNodeDTO = new TestCaseNodeDTO();
            testCaseNodeDTO.setName(name);
            testCaseNodeDTO.setLabel(name);
            testCaseNodeDTO.setChildren(nodeList);
            list.add(testCaseNodeDTO);
        });

        return list;
    }
    
    private List<TestCaseNodeDTO> getNodeDTO(String projectId, String planId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(planId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);

        if (testPlanTestCases.isEmpty()) {
            return null;
        }

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(projectId);
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);

        List<String> caseIds = testPlanTestCases.stream()
                .map(TestPlanTestCase::getCaseId)
                .collect(Collectors.toList());

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(caseIds);
        List<String> dataNodeIds = testCaseMapper.selectByExample(testCaseExample).stream()
                .map(TestCase::getNodeId)
                .collect(Collectors.toList());

        List<TestCaseNodeDTO> nodeTrees = getNodeTrees(nodes);

        Iterator<TestCaseNodeDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            TestCaseNodeDTO rootNode = iterator.next();
            if (pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }

        return nodeTrees;
    }

    /**
     * 去除没有数据的节点
     *
     * @param rootNode
     * @param nodeIds
     * @return 是否剪枝
     */
    public boolean pruningTree(TestCaseNodeDTO rootNode, List<String> nodeIds) {

        List<TestCaseNodeDTO> children = rootNode.getChildren();

        if (children == null || children.isEmpty()) {
            //叶子节点,并且该节点无数据
            if (!nodeIds.contains(rootNode.getId())) {
                return true;
            }
        }

        if (children != null) {
            Iterator<TestCaseNodeDTO> iterator = children.iterator();
            while (iterator.hasNext()) {
                TestCaseNodeDTO subNode = iterator.next();
                if (pruningTree(subNode, nodeIds)) {
                    iterator.remove();
                }
            }

            if (children.isEmpty() && !nodeIds.contains(rootNode.getId())) {
                return true;
            }
        }

        return false;
    }

    public List<TestCaseNodeDTO> getAllNodeByPlanId(QueryNodeRequest request) {
        // todo jenkins 获取模块
        String planId = request.getTestPlanId();
        String projectId = request.getProjectId();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        if (testPlan == null) {
            return Collections.emptyList();
        }

        return getNodeTreeByProjectId(projectId);
    }

    public Map<String, String> createNodeByTestCases(List<TestCaseWithBLOBs> testCases, String projectId) {

        List<TestCaseNodeDTO> nodeTrees = getNodeTreeByProjectId(projectId);

        Map<String, String> pathMap = new HashMap<>();

        List<String> nodePaths = testCases.stream()
                .map(TestCase::getNodePath)
                .collect(Collectors.toList());

        nodePaths.forEach(path -> {

            if (path == null) {
                throw new ExcelException(Translator.get("test_case_module_not_null"));
            }
            List<String> nodeNameList = new ArrayList<>(Arrays.asList(path.split("/")));
            Iterator<String> pathIterator = nodeNameList.iterator();

            Boolean hasNode = false;
            String rootNodeName = null;

            if (nodeNameList.size() <= 1) {
                throw new ExcelException(Translator.get("test_case_create_module_fail") + ":" + path);
            } else {
                pathIterator.next();
                pathIterator.remove();

                rootNodeName = pathIterator.next().trim();
                //原来没有，新建的树nodeTrees也不包含
                for (TestCaseNodeDTO nodeTree : nodeTrees) {
                    if (StringUtils.equals(rootNodeName, nodeTree.getName())) {
                        hasNode = true;
                        createNodeByPathIterator(pathIterator, "/" + rootNodeName, nodeTree,
                                pathMap, projectId, 2);
                    }
                    ;
                }
            }


            if (!hasNode) {
                createNodeByPath(pathIterator, rootNodeName, null, projectId, 1, "", pathMap);
            }
        });

        return pathMap;

    }

    /**
     * 根据目标节点路径，创建相关节点
     *
     * @param pathIterator 遍历子路径
     * @param path         当前路径
     * @param treeNode     当前节点
     * @param pathMap      记录节点路径对应的nodeId
     */
    private void createNodeByPathIterator(Iterator<String> pathIterator, String path, TestCaseNodeDTO treeNode,
                                          Map<String, String> pathMap, String projectId, Integer level) {

        List<TestCaseNodeDTO> children = treeNode.getChildren();

        if (children == null || children.isEmpty() || !pathIterator.hasNext()) {
            pathMap.put(path, treeNode.getId());
            if (pathIterator.hasNext()) {
                createNodeByPath(pathIterator, pathIterator.next().trim(), treeNode, projectId, level, path, pathMap);
            }
            return;
        }

        String nodeName = pathIterator.next().trim();

        Boolean hasNode = false;

        for (TestCaseNodeDTO child : children) {
            if (StringUtils.equals(nodeName, child.getName())) {
                hasNode = true;
                createNodeByPathIterator(pathIterator, path + "/" + child.getName(),
                        child, pathMap, projectId, level + 1);
            }
            ;
        }

        //若子节点中不包含该目标节点，则在该节点下创建
        if (!hasNode) {
            createNodeByPath(pathIterator, nodeName, treeNode, projectId, level, path, pathMap);
        }

    }

    /**
     * @param pathIterator 迭代器，遍历子节点
     * @param nodeName     当前节点
     * @param pNode        父节点
     */
    private void createNodeByPath(Iterator<String> pathIterator, String nodeName,
                                  TestCaseNodeDTO pNode, String projectId, Integer level,
                                  String rootPath, Map<String, String> pathMap) {

        StringBuilder path = new StringBuilder(rootPath);

        path.append("/" + nodeName);

        String pid = null;
        //创建过不创建
        if (pathMap.get(path.toString()) != null) {
            pid = pathMap.get(path.toString());
            level++;
        } else {
            pid = insertTestCaseNode(nodeName, pNode == null ? null : pNode.getId(), projectId, level);
            pathMap.put(path.toString(), pid);
            level++;
        }

        while (pathIterator.hasNext()) {
            String nextNodeName = pathIterator.next();
            path.append("/" + nextNodeName);
            if (pathMap.get(path.toString()) != null) {
                pid = pathMap.get(path.toString());
                level++;
            } else {
                pid = insertTestCaseNode(nextNodeName, pid, projectId, level);
                pathMap.put(path.toString(), pid);
                level++;
            }
        }
    }

    private String insertTestCaseNode(String nodeName, String pId, String projectId, Integer level) {
        TestCaseNode testCaseNode = new TestCaseNode();
        testCaseNode.setName(nodeName.trim());
        testCaseNode.setParentId(pId);
        testCaseNode.setProjectId(projectId);
        testCaseNode.setCreateTime(System.currentTimeMillis());
        testCaseNode.setUpdateTime(System.currentTimeMillis());
        testCaseNode.setLevel(level);
        testCaseNode.setId(UUID.randomUUID().toString());
        testCaseNodeMapper.insert(testCaseNode);
        return testCaseNode.getId();
    }

    public void dragNode(DragNodeRequest request) {

        checkTestCaseNodeExist(request);

        List<String> nodeIds = request.getNodeIds();

        List<TestCaseDTO> testCases = QueryTestCaseByNodeIds(nodeIds);

        TestCaseNodeDTO nodeTree = request.getNodeTree();

        List<TestCaseNode> updateNodes = new ArrayList<>();

        buildUpdateTestCase(nodeTree, testCases, updateNodes, "/", "0", 1);

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateTestCaseNode(updateNodes);

        batchUpdateTestCase(testCases);
    }

    private void batchUpdateTestCaseNode(List<TestCaseNode> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseNodeMapper testCaseNodeMapper = sqlSession.getMapper(TestCaseNodeMapper.class);
        updateNodes.forEach((value) -> {
            testCaseNodeMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
    }

    private void batchUpdateTestCase(List<TestCaseDTO> testCases) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestCaseMapper testCaseMapper = sqlSession.getMapper(TestCaseMapper.class);
        testCases.forEach((value) -> {
            testCaseMapper.updateByPrimaryKey(value);
        });
        sqlSession.flushStatements();
    }

    private List<TestCaseDTO> QueryTestCaseByNodeIds(List<String> nodeIds) {
        QueryTestCaseRequest testCaseRequest = new QueryTestCaseRequest();
        testCaseRequest.setNodeIds(nodeIds);
        return extTestCaseMapper.list(testCaseRequest);
    }

    private void buildUpdateTestCase(TestCaseNodeDTO rootNode, List<TestCaseDTO> testCases,
                                     List<TestCaseNode> updateNodes, String rootPath, String pId, int level) {

        rootPath = rootPath + rootNode.getName();

        if (level > 5) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }

        TestCaseNode testCaseNode = new TestCaseNode();
        testCaseNode.setId(rootNode.getId());
        testCaseNode.setLevel(level);
        testCaseNode.setParentId(pId);
        updateNodes.add(testCaseNode);

        for (TestCaseDTO item : testCases) {
            if (StringUtils.equals(item.getNodeId(), rootNode.getId())) {
                item.setNodePath(rootPath);
            }
        }

        List<TestCaseNodeDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateTestCase(children.get(i), testCases, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
            }
        }
    }

}
