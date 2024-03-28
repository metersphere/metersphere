package io.metersphere.api.service.debug;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiDebugRequest;
import io.metersphere.api.dto.debug.ApiTreeNode;
import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.debug.ModuleUpdateRequest;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.mapper.ApiDebugModuleMapper;
import io.metersphere.api.mapper.ExtApiDebugModuleMapper;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.service.ModuleTreeService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDebugModuleService extends ModuleTreeService {
    private static final String UNPLANNED = "api_debug_module.unplanned_request";
    private static final String MODULE_NO_EXIST = "api_module.not.exist";
    private static final String METHOD = "method";
    private static final String PROTOCOL = "protocol";
    private static final String DEBUG_MODULE_COUNT_ALL = "all";
    @Resource
    private ApiDebugModuleLogService apiDebugModuleLogService;
    @Resource
    private ApiDebugModuleMapper apiDebugModuleMapper;
    @Resource
    private ExtApiDebugModuleMapper extApiDebugModuleMapper;
    @Resource
    private ApiDebugMapper apiDebugMapper;
    @Resource
    private ApiDebugBlobMapper apiDebugBlobMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiFileResourceService apiFileResourceService;

    public List<BaseTreeNode> getTree(String userId) {
        List<BaseTreeNode> fileModuleList = extApiDebugModuleMapper.selectBaseByProtocolAndUser(userId);
        List<BaseTreeNode> baseTreeNodes = super.buildTreeAndCountResource(fileModuleList, true, Translator.get(UNPLANNED));
        List<ApiTreeNode> apiTreeNodeList = extApiDebugModuleMapper.selectApiDebugByProtocolAndUser(userId);
        return getBaseTreeNodes(apiTreeNodeList, baseTreeNodes);

    }

    public List<BaseTreeNode> getBaseTreeNodes(List<ApiTreeNode> apiTreeNodeList, List<BaseTreeNode> baseTreeNodes) {
        if (CollectionUtils.isEmpty(apiTreeNodeList)) {
            return baseTreeNodes;
        }
        List<BaseTreeNode> nodeList = apiTreeNodeList.stream().map(apiTreeNode -> {
            BaseTreeNode baseTreeNode = new BaseTreeNode();
            baseTreeNode.setId(apiTreeNode.getId());
            baseTreeNode.setName(apiTreeNode.getName());
            baseTreeNode.setParentId(apiTreeNode.getParentId());
            baseTreeNode.setType(apiTreeNode.getType());
            if (StringUtils.equals(apiTreeNode.getProtocol(), ModuleConstants.NODE_PROTOCOL_HTTP)) {
                baseTreeNode.putAttachInfo(METHOD, apiTreeNode.getMethod());
                baseTreeNode.putAttachInfo(PROTOCOL, apiTreeNode.getProtocol());
            } else {
                baseTreeNode.putAttachInfo(PROTOCOL, apiTreeNode.getProtocol());
            }
            return baseTreeNode;
        }).toList();
        //apiTreeNodeList使用stream实现将parentId分组生成map
        Map<String, List<BaseTreeNode>> apiTreeNodeMap = nodeList.stream().collect(Collectors.groupingBy(BaseTreeNode::getParentId));
        //遍历baseTreeNodes，将apiTreeNodeMap中的id相等的数据添加到baseTreeNodes中
        return generateTree(baseTreeNodes, apiTreeNodeMap);
    }

    //生成树结构
    private List<BaseTreeNode> generateTree(List<BaseTreeNode> baseTreeNodes, Map<String, List<BaseTreeNode>> apiTreeNodeMap) {
        baseTreeNodes.forEach(baseTreeNode -> {
            if (apiTreeNodeMap.containsKey(baseTreeNode.getId())) {
                baseTreeNode.getChildren().addAll(apiTreeNodeMap.get(baseTreeNode.getId()));
            }
            if (CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                generateTree(baseTreeNode.getChildren(), apiTreeNodeMap);
            }
        });
        return baseTreeNodes;
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String userId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extApiDebugModuleMapper.selectIdAndParentIdByProtocolAndUserId(userId);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get(UNPLANNED));
    }

    public String add(ModuleCreateRequest request, String operator) {
        ApiDebugModule apiDebugModule = new ApiDebugModule();
        apiDebugModule.setId(IDGenerator.nextStr());
        apiDebugModule.setName(request.getName());
        apiDebugModule.setParentId(request.getParentId());
        apiDebugModule.setProjectId(request.getProjectId());
        apiDebugModule.setCreateUser(operator);
        this.checkDataValidity(apiDebugModule);
        apiDebugModule.setCreateTime(System.currentTimeMillis());
        apiDebugModule.setUpdateTime(apiDebugModule.getCreateTime());
        apiDebugModule.setPos(this.countPos(request.getParentId()));
        apiDebugModule.setUpdateUser(operator);
        apiDebugModuleMapper.insert(apiDebugModule);
        //记录日志
        apiDebugModuleLogService.saveAddLog(apiDebugModule, operator);
        return apiDebugModule.getId();
    }

    private Long countPos(String parentId) {
        Long maxPos = extApiDebugModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    /**
     * 检查数据的合法性
     */
    private void checkDataValidity(ApiDebugModule apiDebugModule) {
        ApiDebugModuleExample example = new ApiDebugModuleExample();
        if (!StringUtils.equals(apiDebugModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //检查父ID是否存在  调试模块的逻辑是  同一个用户下的同级模块不能重名  每个协议是不同的模块
            example.createCriteria().andIdEqualTo(apiDebugModule.getParentId())
                    .andCreateUserEqualTo(apiDebugModule.getCreateUser());
            if (apiDebugModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();
        }
        example.createCriteria().andParentIdEqualTo(apiDebugModule.getParentId())
                .andNameEqualTo(apiDebugModule.getName())
                .andIdNotEqualTo(apiDebugModule.getId())
                .andCreateUserEqualTo(apiDebugModule.getCreateUser());
        if (apiDebugModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
    }

    public void update(ModuleUpdateRequest request, String userId, String projectId) {
        ApiDebugModule module = checkModuleExist(request.getId());
        ApiDebugModule updateModule = new ApiDebugModule();
        updateModule.setId(request.getId());
        updateModule.setName(request.getName());
        updateModule.setParentId(module.getParentId());
        updateModule.setCreateUser(module.getCreateUser());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        updateModule.setProjectId(projectId);
        apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);
        //记录日志
        apiDebugModuleLogService.saveUpdateLog(updateModule, projectId, userId);
    }

    public ApiDebugModule checkModuleExist(String moduleId) {
        ApiDebugModule module = apiDebugModuleMapper.selectByPrimaryKey(moduleId);
        if (module == null) {
            throw new MSException(Translator.get(MODULE_NO_EXIST));
        }
        return module;
    }


    public void deleteModule(String deleteId, String currentUser) {
        ApiDebugModule deleteModule = checkModuleExist(deleteId);
        if (deleteModule != null) {
            this.deleteModule(Collections.singletonList(deleteId), currentUser, deleteModule.getProjectId());
        }
    }

    public void deleteModule(List<String> deleteIds, String currentUser, String projectId) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        List<BaseTreeNode> baseTreeNodes = extApiDebugModuleMapper.selectBaseNodeByIds(deleteIds);
        extApiDebugModuleMapper.deleteByIds(deleteIds);
        apiDebugModuleLogService.saveDeleteModuleLog(baseTreeNodes, currentUser, projectId);
        //删除模块下的所有接口
        ApiDebugExample example = new ApiDebugExample();
        example.createCriteria().andModuleIdIn(deleteIds);
        List<ApiDebug> apiDebugs = apiDebugMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiDebugs)) {
            List<String> apiDebugIds = apiDebugs.stream().map(ApiDebug::getId).toList();
            apiDebugMapper.deleteByExample(example);
            ApiDebugBlobExample blobExample = new ApiDebugBlobExample();
            blobExample.createCriteria().andIdIn(apiDebugIds);
            apiDebugBlobMapper.deleteByExample(blobExample);
            //删除文件关联关系
            String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(projectId, StringUtils.EMPTY);
            apiFileResourceService.deleteByResourceIds(apiDebugDir, apiDebugIds, projectId, currentUser, OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG);

            apiDebugModuleLogService.saveDeleteDataLog(apiDebugs, currentUser, projectId);
        }

        List<String> childrenIds = extApiDebugModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds, currentUser, projectId);
        }
    }

    public void moveNode(NodeMoveRequest request, String currentUser) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                extApiDebugModuleMapper::selectBaseModuleById,
                extApiDebugModuleMapper::selectModuleByParentIdAndPosOperator);

        ApiDebugModuleExample example = new ApiDebugModuleExample();
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andIdEqualTo(request.getDragNodeId());
        //节点换到了别的节点下,要先更新parent节点.
        if (apiDebugModuleMapper.countByExample(example) == 0) {
            ApiDebugModule fileModule = new ApiDebugModule();
            ApiDebugModule currentModule = apiDebugModuleMapper.selectByPrimaryKey(request.getDragNodeId());
            currentModule.setParentId(nodeSortDTO.getParent().getId());
            checkDataValidity(currentModule);
            fileModule.setId(request.getDragNodeId());
            fileModule.setParentId(nodeSortDTO.getParent().getId());
            apiDebugModuleMapper.updateByPrimaryKeySelective(fileModule);
        }

        super.sort(nodeSortDTO);
        //记录日志
        apiDebugModuleLogService.saveMoveLog(nodeSortDTO, currentUser);
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     */
    public Map<String, Long> getModuleCountMap(String userId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(userId, moduleCountDTOList);
        return super.getIdCountMapByBreadth(treeNodeList);
    }


    @Override
    public void updatePos(String id, long pos) {
        ApiDebugModule updateModule = new ApiDebugModule();
        updateModule.setPos(pos);
        updateModule.setId(id);
        apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = extApiDebugModuleMapper.selectChildrenIdsSortByPos(parentId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDebugModuleMapper batchUpdateMapper = sqlSession.getMapper(ApiDebugModuleMapper.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            ApiDebugModule updateModule = new ApiDebugModule();
            updateModule.setId(nodeId);
            updateModule.setPos((i + 1) * LIMIT_POS);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModule);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public Map<String, Long> moduleCount(ApiDebugRequest request, String operator) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = extApiDebugModuleMapper.countModuleIdByKeywordAndProtocol(request, operator);
        long allCount = getAllCount(moduleCountDTOList);
        Map<String, Long> moduleCountMap = getModuleCountMap(operator, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

}
