package io.metersphere.api.service;


import io.metersphere.api.dto.delimit.ApiDelimitRequest;
import io.metersphere.api.dto.delimit.ApiDelimitResult;
import io.metersphere.api.dto.delimit.ApiModuleDTO;
import io.metersphere.api.dto.delimit.DragModuleRequest;
import io.metersphere.base.domain.ApiDelimitExample;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiModuleExample;
import io.metersphere.base.mapper.ApiDelimitMapper;
import io.metersphere.base.mapper.ApiModuleMapper;
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
    private ApiDelimitMapper apiDelimitMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;

    public List<ApiModuleDTO> getNodeTreeByProjectId(String projectId, String protocol) {
        ApiModuleExample apiDelimitNodeExample = new ApiModuleExample();
        apiDelimitNodeExample.createCriteria().andProjectIdEqualTo(projectId);
        apiDelimitNodeExample.createCriteria().andProtocolEqualTo(protocol);
        apiDelimitNodeExample.setOrderByClause("create_time asc");
        List<ApiModule> nodes = apiModuleMapper.selectByExample(apiDelimitNodeExample);
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
                MSException.throwException(Translator.get("test_case_module_already_exists"));
            }
        }
    }

    private List<ApiDelimitResult> queryByModuleIds(List<String> nodeIds) {
        ApiDelimitRequest apiDelimitRequest = new ApiDelimitRequest();
        apiDelimitRequest.setModuleIds(nodeIds);
        return apiDelimitMapper.list(apiDelimitRequest);
    }

    public int editNode(DragModuleRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        checkApiModuleExist(request);
        List<ApiDelimitResult> apiModule = queryByModuleIds(request.getNodeIds());

        apiModule.forEach(apiDelimit -> {
            StringBuilder path = new StringBuilder(apiDelimit.getModulePath());
            List<String> pathLists = Arrays.asList(path.toString().split("/"));
            pathLists.set(request.getLevel(), request.getName());
            path.delete(0, path.length());
            for (int i = 1; i < pathLists.size(); i++) {
                path = path.append("/").append(pathLists.get(i));
            }
            apiDelimit.setModulePath(path.toString());
        });

        batchUpdateApiDelimit(apiModule);

        return apiModuleMapper.updateByPrimaryKeySelective(request);
    }

    public int deleteNode(List<String> nodeIds) {
        ApiDelimitExample apiDelimitExample = new ApiDelimitExample();
        apiDelimitExample.createCriteria().andModuleIdIn(nodeIds);
        apiDelimitMapper.deleteByExample(apiDelimitExample);

        ApiModuleExample apiDelimitNodeExample = new ApiModuleExample();
        apiDelimitNodeExample.createCriteria().andIdIn(nodeIds);
        return apiModuleMapper.deleteByExample(apiDelimitNodeExample);
    }

    private void batchUpdateApiDelimit(List<ApiDelimitResult> apiModule) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDelimitMapper apiDelimitMapper = sqlSession.getMapper(ApiDelimitMapper.class);
        apiModule.forEach((value) -> {
            apiDelimitMapper.updateByPrimaryKey(value);
        });
        sqlSession.flushStatements();
    }

    public void dragNode(DragModuleRequest request) {

        checkApiModuleExist(request);

        List<String> nodeIds = request.getNodeIds();

        List<ApiDelimitResult> apiModule = queryByModuleIds(nodeIds);

        ApiModuleDTO nodeTree = request.getNodeTree();

        List<ApiModule> updateNodes = new ArrayList<>();

        buildUpdateDelimit(nodeTree, apiModule, updateNodes, "/", "0", nodeTree.getLevel());

        updateNodes = updateNodes.stream()
                .filter(item -> nodeIds.contains(item.getId()))
                .collect(Collectors.toList());

        batchUpdateModule(updateNodes);

        batchUpdateApiDelimit(apiModule);
    }

    private void buildUpdateDelimit(ApiModuleDTO rootNode, List<ApiDelimitResult> apiDelimits,
                                    List<ApiModule> updateNodes, String rootPath, String pId, int level) {

        rootPath = rootPath + rootNode.getName();

        if (level > 8) {
            MSException.throwException(Translator.get("node_deep_limit"));
        }
        if (rootNode.getId().equals("rootID")) {
            rootPath = "";
        }
        ApiModule apiDelimitNode = new ApiModule();
        apiDelimitNode.setId(rootNode.getId());
        apiDelimitNode.setLevel(level);
        apiDelimitNode.setParentId(pId);
        updateNodes.add(apiDelimitNode);

        for (ApiDelimitResult item : apiDelimits) {
            if (StringUtils.equals(item.getModuleId(), rootNode.getId())) {
                item.setModulePath(rootPath);
            }
        }

        List<ApiModuleDTO> children = rootNode.getChildren();
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                buildUpdateDelimit(children.get(i), apiDelimits, updateNodes, rootPath + '/', rootNode.getId(), level + 1);
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
