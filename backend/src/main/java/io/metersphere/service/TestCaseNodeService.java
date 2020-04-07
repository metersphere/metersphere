package io.metersphere.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseNodeMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.dto.TestCaseNodeDTO;
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

        if(node.getLevel() > 5){
            throw new RuntimeException("模块树最大深度为5层！");
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
            if (node.getpId().equals(rootNode.getId())){
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
}
