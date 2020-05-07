package io.metersphere.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseNodeMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.dto.TestCaseNodeDTO;
import io.metersphere.exception.ExcelException;
import org.apache.commons.lang3.StringUtils;
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

    public int addNode(TestCaseNode node) {

        if(node.getLevel() > TestCaseConstants.MAX_NODE_DEPTH){
            throw new RuntimeException("模块树最大深度为" + TestCaseConstants.MAX_NODE_DEPTH + "层！");
        }
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        testCaseNodeMapper.insertSelective(node);
        return node.getId();
    }

    public List<TestCaseNodeDTO> getNodeTreeByProjectId(String projectId) {
        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(projectId);
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);
        return getNodeTrees(nodes);
    }

    private List<TestCaseNodeDTO> getNodeTrees(List<TestCaseNode> nodes) {

        List<TestCaseNodeDTO> nodeTreeList = new ArrayList<>();

        Map<Integer, List<TestCaseNode>> nodeLevelMap = new HashMap<>();

        nodes.forEach(node -> {
            Integer level = node.getLevel();
            if( nodeLevelMap.containsKey(level) ){
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
     * @param nodeLevelMap
     * @param rootNode
     * @return
     */
    private TestCaseNodeDTO buildNodeTree(Map<Integer,List<TestCaseNode>> nodeLevelMap, TestCaseNode rootNode) {

        TestCaseNodeDTO nodeTree = new TestCaseNodeDTO();
        BeanUtils.copyBean(nodeTree, rootNode);
        nodeTree.setLabel(rootNode.getName());

        List<TestCaseNode> lowerNodes = nodeLevelMap.get(rootNode.getLevel() + 1);
        if(lowerNodes == null){
            return nodeTree;
        }

        List<TestCaseNodeDTO> childrens = Optional.ofNullable(nodeTree.getChildren()).orElse(new ArrayList<>());

        lowerNodes.forEach(node -> {
            if (node.getPId().equals(rootNode.getId())){
                childrens.add(buildNodeTree(nodeLevelMap, node));
                nodeTree.setChildren(childrens);
            }
        });

        return nodeTree;
    }

    public int editNode(TestCaseNode node) {
        node.setUpdateTime(System.currentTimeMillis());
        return testCaseNodeMapper.updateByPrimaryKeySelective(node);
    }

    public int deleteNode(List<Integer> nodeIds) {
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
     * @param planId
     * @return
     */
    public List<TestCaseNodeDTO> getNodeByPlanId(String planId) {

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);

        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(planId);
        List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);

        if (testPlanTestCases.isEmpty()) {
            return null;
        }

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(testPlan.getProjectId());
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);

        List<String> caseIds = testPlanTestCases.stream()
                .map(TestPlanTestCase::getCaseId)
                .collect(Collectors.toList());

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(caseIds);
        List<Integer> dataNodeIds = testCaseMapper.selectByExample(testCaseExample).stream()
                .map(TestCase::getNodeId)
                .collect(Collectors.toList());

        List<TestCaseNodeDTO> nodeTrees = getNodeTrees(nodes);

        Iterator<TestCaseNodeDTO> iterator = nodeTrees.iterator();
        while(iterator.hasNext()){
            TestCaseNodeDTO rootNode = iterator.next();
            if(pruningTree(rootNode, dataNodeIds)){
                iterator.remove();
            }
        }

        return nodeTrees;
    }

    /**
     * 去除没有数据的节点
     * @param rootNode
     * @param nodeIds
     * @return 是否剪枝
     * */
    public boolean pruningTree(TestCaseNodeDTO rootNode, List<Integer> nodeIds) {

        List<TestCaseNodeDTO> children = rootNode.getChildren();

        if(children == null || children.isEmpty()){
            //叶子节点,并且该节点无数据
            if(!nodeIds.contains(rootNode.getId())){
                return true;
            }
        }

        if(children != null) {
            Iterator<TestCaseNodeDTO> iterator = children.iterator();
            while(iterator.hasNext()){
                TestCaseNodeDTO subNode = iterator.next();
                if(pruningTree(subNode, nodeIds)){
                    iterator.remove();
                }
            }

            if (children.isEmpty() && !nodeIds.contains(rootNode.getId())) {
                return true;
            }
        }

        return false;
    }

    public List<TestCaseNodeDTO> getAllNodeByPlanId(String planId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        return getNodeTreeByProjectId(testPlan.getProjectId());
    }

    public Map<String, Integer> createNodeByTestCases(List<TestCaseWithBLOBs> testCases, String projectId) {

        List<TestCaseNodeDTO> nodeTrees = getNodeTreeByProjectId(projectId);

        Map<String, Integer> pathMap = new HashMap<>();

        List<String> nodePaths = testCases.stream()
                .map(TestCase::getNodePath)
                .collect(Collectors.toList());

        nodePaths.forEach(path -> {

            if (path == null) {
                throw new ExcelException("所属模块不能为空！");
            }
            List<String> nodeNameList = new ArrayList<>(Arrays.asList(path.split("/")));
            Iterator<String> pathIterator = nodeNameList.iterator();

            Boolean hasNode = false;
            String rootNodeName = null;

            if (nodeNameList.size() <= 1) {
                throw new ExcelException("创建模块失败：" + path);
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
                    };
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
     * @param pathIterator 遍历子路径
     * @param path 当前路径
     * @param treeNode 当前节点
     * @param pathMap 记录节点路径对应的nodeId
     */
    private void createNodeByPathIterator(Iterator<String> pathIterator, String path, TestCaseNodeDTO treeNode,
                                  Map<String, Integer> pathMap, String projectId, Integer level) {

        List<TestCaseNodeDTO> children = treeNode.getChildren();

        if (children == null || children.isEmpty() || !pathIterator.hasNext()) {
            pathMap.put(path , treeNode.getId());
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
            };
        }

        //若子节点中不包含该目标节点，则在该节点下创建
        if (!hasNode) {
            createNodeByPath(pathIterator, nodeName, treeNode, projectId, level, path, pathMap);
        }

    }

    /**
     *
     * @param pathIterator 迭代器，遍历子节点
     * @param nodeName 当前节点
     * @param pNode 父节点
     */
    private void createNodeByPath(Iterator<String> pathIterator, String nodeName,
                                  TestCaseNodeDTO pNode, String projectId, Integer level,
                                  String rootPath, Map<String, Integer> pathMap) {

        StringBuilder path = new StringBuilder(rootPath);

        path.append("/" + nodeName);

        Integer pid = null;
        //创建过不创建
        if (pathMap.get(path.toString()) != null) {
            pid = pathMap.get(path.toString());
            level++;
        } else {
            pid = insertTestCaseNode(nodeName, pNode == null ? null : pNode.getId(), projectId, level);
            pathMap.put(path.toString(), pid);
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
            }
        }
    }

    private Integer insertTestCaseNode(String nodeName, Integer pId, String projectId, Integer level) {
        TestCaseNode testCaseNode = new TestCaseNode();
        testCaseNode.setName(nodeName.trim());
        testCaseNode.setPId(pId);
        testCaseNode.setProjectId(projectId);
        testCaseNode.setCreateTime(System.currentTimeMillis());
        testCaseNode.setUpdateTime(System.currentTimeMillis());
        testCaseNode.setLevel(level);
        testCaseNodeMapper.insert(testCaseNode);
        return testCaseNode.getId();
    }
}
