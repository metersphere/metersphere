package io.metersphere.api.service;


import io.metersphere.api.dto.definition.ApiDefinitionRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.ApiModuleDTO;
import io.metersphere.api.dto.definition.DragModuleRequest;
import io.metersphere.base.domain.ApiDefinitionExample;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiModuleExample;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.i18n.Translator;
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
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    public List<ApiModuleDTO> getNodeTreeByProjectId(String projectId, String protocol) {
        ApiModuleExample apiDefinitionNodeExample = new ApiModuleExample();
        apiDefinitionNodeExample.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol);
        apiDefinitionNodeExample.setOrderByClause("create_time asc");
        List<ApiModule> nodes = apiModuleMapper.selectByExample(apiDefinitionNodeExample);
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
        checkApiModuleExist(node);
    }

    private void checkApiModuleExist(ApiModule node) {
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
                MSException.throwException(Translator.get("test_case_module_already_exists") + ": " + node.getName());
            }
        }
    }

    private List<ApiDefinitionResult> queryByModuleIds(List<String> nodeIds) {
        ApiDefinitionRequest apiDefinitionRequest = new ApiDefinitionRequest();
        apiDefinitionRequest.setModuleIds(nodeIds);
        return extApiDefinitionMapper.list(apiDefinitionRequest);
    }

    public int editNode(DragModuleRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        checkApiModuleExist(request);
        List<ApiDefinitionResult> apiModule = queryByModuleIds(request.getNodeIds());

        apiModule.forEach(apiDefinition -> {
            StringBuilder path = new StringBuilder(apiDefinition.getModulePath());
            List<String> pathLists = Arrays.asList(path.toString().split("/"));
            pathLists.set(request.getLevel(), request.getName());
            path.delete(0, path.length());
            for (int i = 1; i < pathLists.size(); i++) {
                path = path.append("/").append(pathLists.get(i));
            }
            apiDefinition.setModulePath(path.toString());
        });

        batchUpdateApiDefinition(apiModule);

        return apiModuleMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andModuleIdIn(nodeIds);
        apiDefinitionMapper.deleteByExample(apiDefinitionExample);

        ApiModuleExample apiDefinitionNodeExample = new ApiModuleExample();
        apiDefinitionNodeExample.createCriteria().andIdIn(nodeIds);
        return apiModuleMapper.deleteByExample(apiDefinitionNodeExample);
    }

    private void batchUpdateApiDefinition(List<ApiDefinitionResult> apiModule) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMapper apiDefinitionMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        apiModule.forEach((value) -> {
            apiDefinitionMapper.updateByPrimaryKey(value);
        });
        sqlSession.flushStatements();
    }

    public void dragNode(DragModuleRequest request) {

        checkApiModuleExist(request);

        List<String> nodeIds = request.getNodeIds();

        List<ApiDefinitionResult> apiModule = queryByModuleIds(nodeIds);

        ApiModuleDTO nodeTree = request.getNodeTree();

        List<ApiModule> updateNodes = new ArrayList<>();

        buildUpdateDefinition(nodeTree, apiModule, updateNodes, "/", "0", nodeTree.getLevel());

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateModule(updateNodes);

        batchUpdateApiDefinition(apiModule);
    }

    private void buildUpdateDefinition(ApiModuleDTO rootNode, List<ApiDefinitionResult> apiDefinitions,
                                    List<ApiModule> updateNodes, String rootPath, String pId, int level) {

        rootPath = rootPath + rootNode.getName();

        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }
        if (rootNode.getId().equals("root")) {
            rootPath = "";
        }
        ApiModule apiDefinitionNode = new ApiModule();
        apiDefinitionNode.setId(rootNode.getId());
        apiDefinitionNode.setLevel(level);
        apiDefinitionNode.setParentId(pId);
        updateNodes.add(apiDefinitionNode);

        for (ApiDefinitionResult item : apiDefinitions) {
            if (StringUtils.equals(item.getModuleId(), rootNode.getId())) {
                item.setModulePath(rootPath);
            }
        }

        List<ApiModuleDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateDefinition(children.get(i), apiDefinitions, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
            }
        }
    }

    private void batchUpdateModule(List<ApiModule> updateNodes) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiModuleMapper apiModuleMapper = sqlSession.getMapper(ApiModuleMapper.class);
        updateNodes.forEach((value) -> {
            apiModuleMapper.updateByPrimaryKeySelective(value);
        });
        sqlSession.flushStatements();
    }


}
