package io.metersphere.service;


import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.base.domain.TestCaseNodeExample;
import io.metersphere.base.mapper.TestCaseNodeMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.dto.TestCaseNodeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseNodeService {

    @Resource
    TestCaseNodeMapper testCaseNodeMapper;

    public int addNode(TestCaseNode node) {

        if(node.getLevel() > 5){
            throw new RuntimeException("模块树最大深度为5层！");
        }
        node.setCreateTime(System.currentTimeMillis());
        node.setUpdateTime(System.currentTimeMillis());
        testCaseNodeMapper.insertSelective(node);
        return node.getId();
    }

    public List<TestCaseNodeDTO> getNodeByProjectId(String projectId) {

        List<TestCaseNodeDTO> nodeTreeList = new ArrayList<>();

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(projectId);
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);

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

        List<TestCaseNode> testCaseNodes = nodeLevelMap.get(rootNode.getLevel() + 1);
        if(testCaseNodes == null){
            return nodeTree;
        }

        List<TestCaseNodeDTO> childrens = Optional.ofNullable(nodeTree.getChildren()).orElse(new ArrayList<>());

        testCaseNodes.forEach(node -> {
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

    public int deleteNode(int nodeId) {
        return testCaseNodeMapper.deleteByPrimaryKey(nodeId);
    }
}
