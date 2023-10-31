package io.metersphere.api.service.debug;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiDebugRequest;
import io.metersphere.api.dto.debug.ApiTreeNode;
import io.metersphere.api.dto.debug.DebugModuleCreateRequest;
import io.metersphere.api.dto.debug.DebugModuleUpdateRequest;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.mapper.ApiDebugModuleMapper;
import io.metersphere.api.mapper.ExtApiDebugModuleMapper;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.service.ModuleTreeService;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDebugModuleService extends ModuleTreeService {
    private static final String UNPLANNED = "api_debug_module.unplanned_request";
    private static final String MODULE_NO_EXIST = "api_module.not.exist";
    private static final String METHOD = "method";
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

    public List<BaseTreeNode> getTree(String protocol, String userId) {
        List<BaseTreeNode> fileModuleList = extApiDebugModuleMapper.selectBaseByProtocolAndUser(protocol, userId);
        List<BaseTreeNode> baseTreeNodes = super.buildTreeAndCountResource(fileModuleList, true, Translator.get(UNPLANNED));
        List<ApiTreeNode> apiTreeNodeList = extApiDebugModuleMapper.selectApiDebugByProtocolAndUser(protocol, userId);
        //将apiTreeNodeList转换成BaseTreeNode  method放入map中
        if (CollectionUtils.isEmpty(apiTreeNodeList)) {
            return baseTreeNodes;
        }
        List<BaseTreeNode> nodeList = apiTreeNodeList.stream().map(apiTreeNode -> {
            BaseTreeNode baseTreeNode = new BaseTreeNode();
            baseTreeNode.setId(apiTreeNode.getId());
            baseTreeNode.setName(apiTreeNode.getName());
            baseTreeNode.setParentId(apiTreeNode.getParentId());
            baseTreeNode.setType(apiTreeNode.getType());
            baseTreeNode.putAttachInfo(METHOD, apiTreeNode.getMethod());
            return baseTreeNode;
        }).toList();
        //apiTreeNodeList使用stream实现将parentId分组生成map
        Map<String, List<BaseTreeNode>> apiTreeNodeMap = nodeList.stream().collect(Collectors.groupingBy(BaseTreeNode::getParentId));
        //遍历baseTreeNodes，将apiTreeNodeMap中的id相等的数据添加到baseTreeNodes中
        return generateTree(baseTreeNodes, apiTreeNodeMap);

    }

    //生成树结构
    public List<BaseTreeNode> generateTree(List<BaseTreeNode> baseTreeNodes, Map<String, List<BaseTreeNode>> apiTreeNodeMap) {
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

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String protocol, String userId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extApiDebugModuleMapper.selectIdAndParentIdByProtocolAndUserId(protocol, userId);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get(UNPLANNED));
    }

    public String add(DebugModuleCreateRequest request, String operator) {
        ApiDebugModule apiDebugModule = new ApiDebugModule();
        apiDebugModule.setId(IDGenerator.nextStr());
        apiDebugModule.setName(request.getName());
        apiDebugModule.setParentId(request.getParentId());
        apiDebugModule.setProjectId(request.getProjectId());
        apiDebugModule.setProtocol(request.getProtocol());
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
                    .andCreateUserEqualTo(apiDebugModule.getCreateUser())
                    .andProtocolEqualTo(apiDebugModule.getProtocol());
            if (apiDebugModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();
        }
        example.createCriteria().andParentIdEqualTo(apiDebugModule.getParentId())
                .andNameEqualTo(apiDebugModule.getName())
                .andIdNotEqualTo(apiDebugModule.getId())
                .andCreateUserEqualTo(apiDebugModule.getCreateUser())
                .andProtocolEqualTo(apiDebugModule.getProtocol());
        if (apiDebugModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
    }

    public void update(DebugModuleUpdateRequest request, String userId, String projectId) {
        ApiDebugModule module = apiDebugModuleMapper.selectByPrimaryKey(request.getId());
        if (module == null) {
            throw new MSException(Translator.get(MODULE_NO_EXIST));
        }
        ApiDebugModule updateModule = new ApiDebugModule();
        updateModule.setId(request.getId());
        updateModule.setName(request.getName());
        updateModule.setParentId(module.getParentId());
        updateModule.setProtocol(module.getProtocol());
        updateModule.setCreateUser(module.getCreateUser());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        updateModule.setProjectId(projectId);
        apiDebugModuleMapper.updateByPrimaryKeySelective(updateModule);
        //记录日志
        apiDebugModuleLogService.saveUpdateLog(updateModule, projectId, userId);
    }


    public void deleteModule(String deleteId, String currentUser) {
        ApiDebugModule deleteModule = apiDebugModuleMapper.selectByPrimaryKey(deleteId);
        if (deleteModule != null) {
            this.deleteModule(Collections.singletonList(deleteId));
            //记录日志
            apiDebugModuleLogService.saveDeleteLog(deleteModule, currentUser);
        }
    }

    public void deleteModule(List<String> deleteIds) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        extApiDebugModuleMapper.deleteByIds(deleteIds);
        //删除模块下的所有接口
        ApiDebugExample example = new ApiDebugExample();
        example.createCriteria().andModuleIdIn(deleteIds);
        List<ApiDebug> apiDebugs = apiDebugMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiDebugs)) {
            List<String> apiDebugIds = apiDebugs.stream().map(ApiDebug::getId).collect(Collectors.toList());
            apiDebugMapper.deleteByExample(example);
            ApiDebugBlobExample blobExample = new ApiDebugBlobExample();
            blobExample.createCriteria().andIdIn(apiDebugIds);
            apiDebugBlobMapper.deleteByExample(blobExample);
        }

        List<String> childrenIds = extApiDebugModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds);
        }
    }

    public void moveNode(NodeMoveRequest request, String currentUser) {
        BaseModule module;
        BaseModule parentModule;
        BaseModule previousNode = null;
        BaseModule nextNode = null;

        ApiDebugModule dragNode = apiDebugModuleMapper.selectByPrimaryKey(request.getDragNodeId());
        if (dragNode == null) {
            throw new MSException(Translator.get(MODULE_NO_EXIST) + ": " + request.getDragNodeId());
        } else {
            module = new BaseModule(dragNode.getId(), dragNode.getName(), dragNode.getPos(), dragNode.getProjectId(), dragNode.getParentId());
        }

        if (StringUtils.equals(request.getDragNodeId(), request.getDropNodeId())) {
            //两种节点不能一样
            throw new MSException(Translator.get("invalid_parameter"));
        }

        ApiDebugModule dropNode = apiDebugModuleMapper.selectByPrimaryKey(request.getDropNodeId());
        if (dropNode == null) {
            throw new MSException(Translator.get(MODULE_NO_EXIST) + ": " + request.getDropNodeId());
        }

        if (request.getDropPosition() == 0) {
            //dropPosition=0: 放到dropNode节点内，最后一个节点之后
            parentModule = new BaseModule(dropNode.getId(), dropNode.getName(), dropNode.getPos(), dropNode.getProjectId(), dropNode.getParentId());
            ApiDebugModule previousModule = extApiDebugModuleMapper.getLastModuleByParentId(parentModule.getId());
            if (previousModule != null) {
                previousNode = new BaseModule(previousModule.getId(), previousModule.getName(), previousModule.getPos(), previousModule.getProjectId(), previousModule.getParentId());
            }
        } else {
            if (StringUtils.equals(dropNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                parentModule = new BaseModule(ModuleConstants.ROOT_NODE_PARENT_ID, ModuleConstants.ROOT_NODE_PARENT_ID, 0, module.getProjectId(), ModuleConstants.ROOT_NODE_PARENT_ID);
            } else {
                ApiDebugModule parent = apiDebugModuleMapper.selectByPrimaryKey(dropNode.getParentId());
                parentModule = new BaseModule(parent.getId(), parent.getName(), parent.getPos(), parent.getProjectId(), parent.getParentId());
            }

            if (request.getDropPosition() == 1) {
                //dropPosition=1: 放到dropNode节点后，原dropNode后面的节点之前
                previousNode = new BaseModule(dropNode.getId(), dropNode.getName(), dropNode.getPos(), dropNode.getProjectId(), dropNode.getParentId());
                ApiDebugModule nextModule = extApiDebugModuleMapper.getNextModuleInParentId(previousNode.getParentId(), previousNode.getPos());
                if (nextModule != null) {
                    nextNode = new BaseModule(nextModule.getId(), nextModule.getName(), nextModule.getPos(), nextModule.getProjectId(), nextModule.getParentId());
                }
            } else if (request.getDropPosition() == -1) {
                //dropPosition=-1: 放到dropNode节点前，原dropNode前面的节点之后
                nextNode = new BaseModule(dropNode.getId(), dropNode.getName(), dropNode.getPos(), dropNode.getProjectId(), dropNode.getParentId());
                ApiDebugModule previousModule = extApiDebugModuleMapper.getPreviousModuleInParentId(nextNode.getParentId(), nextNode.getPos());
                if (previousModule != null) {
                    previousNode = new BaseModule(previousModule.getId(), previousModule.getName(), previousModule.getPos(), previousModule.getProjectId(), previousModule.getParentId());
                }
            } else {
                throw new MSException(Translator.get("invalid_parameter"));
            }
        }

        ApiDebugModuleExample example = new ApiDebugModuleExample();
        example.createCriteria().andParentIdEqualTo(parentModule.getId()).andIdEqualTo(module.getId());
        //节点换到了别的节点下,要先更新parent节点.
        if (apiDebugModuleMapper.countByExample(example) == 0) {
            ApiDebugModule fileModule = new ApiDebugModule();
            fileModule.setId(module.getId());
            fileModule.setParentId(parentModule.getId());
            apiDebugModuleMapper.updateByPrimaryKeySelective(fileModule);
        }

        NodeSortDTO nodeMoveDTO = new NodeSortDTO(module, parentModule, previousNode, nextNode);
        super.sort(nodeMoveDTO);

        //记录日志
        apiDebugModuleLogService.saveMoveLog(nodeMoveDTO, currentUser);
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     */
    public Map<String, Long> getModuleCountMap(String protocol, String userId, List<ModuleCountDTO> moduleCountDTOList) {
        Map<String, Long> returnMap = new HashMap<>();
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(protocol, userId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        List<BaseTreeNode> whileList = new ArrayList<>(treeNodeList);
        while (CollectionUtils.isNotEmpty(whileList)) {
            List<BaseTreeNode> childList = new ArrayList<>();
            for (BaseTreeNode treeNode : whileList) {
                returnMap.put(treeNode.getId(), treeNode.getCount());
                childList.addAll(treeNode.getChildren());
            }
            whileList = childList;
        }
        return returnMap;
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
        Map<String, Long> moduleCountMap = getModuleCountMap(request.getProtocol(), operator, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

}
