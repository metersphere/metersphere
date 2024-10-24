package io.metersphere.api.service.scenario;

import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.domain.ApiScenarioModule;
import io.metersphere.api.domain.ApiScenarioModuleExample;
import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.debug.ModuleUpdateRequest;
import io.metersphere.api.dto.scenario.ApiScenarioModuleRequest;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiScenarioModuleMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiScenarioModuleMapper;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.service.ModuleTreeService;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioModuleService extends ModuleTreeService {
    private static final String UNPLANNED_SCENARIO = "api_unplanned_scenario";
    private static final String MODULE_NO_EXIST = "api_module.not.exist";
    private static final String DEBUG_MODULE_COUNT_ALL = "all";
    @Resource
    private ApiScenarioModuleLogService apiScenarioModuleLogService;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ExtApiScenarioModuleMapper extApiScenarioModuleMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;

    public List<BaseTreeNode> getTree(ApiScenarioModuleRequest request) {
        //接口的树结构是  模块：子模块+接口 接口为非delete状态的
        List<BaseTreeNode> fileModuleList = extApiScenarioModuleMapper.selectBaseByRequest(request);
        return super.buildTreeAndCountResource(fileModuleList, true, Translator.get(UNPLANNED_SCENARIO));
    }

    public List<BaseTreeNode> getImportTreeNodeList(String projectId) {

        //接口的树结构是  模块：子模块+接口 接口为非delete状态的
        List<BaseTreeNode> traverseList = extApiScenarioModuleMapper.selectBaseByRequest(new ApiScenarioModuleRequest() {{
            this.setProjectId(projectId);
        }});

        List<BaseTreeNode> baseTreeNodeList = new ArrayList<>();
        BaseTreeNode defaultNode = new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, Translator.get(UNPLANNED_SCENARIO), ModuleConstants.NODE_TYPE_DEFAULT, ModuleConstants.ROOT_NODE_PARENT_ID);
        defaultNode.setPath(StringUtils.join("/", defaultNode.getName()));
        baseTreeNodeList.add(defaultNode);
        int lastSize = 0;
        Map<String, BaseTreeNode> baseTreeNodeMap = new HashMap<>();
        while (CollectionUtils.isNotEmpty(traverseList) && traverseList.size() != lastSize) {
            lastSize = traverseList.size();
            List<BaseTreeNode> notMatchedList = new ArrayList<>();
            for (BaseTreeNode treeNode : traverseList) {
                if (!baseTreeNodeMap.containsKey(treeNode.getParentId()) && !StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    notMatchedList.add(treeNode);
                    continue;
                }
                BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                node.genModulePath(baseTreeNodeMap.get(treeNode.getParentId()));
                baseTreeNodeMap.put(treeNode.getId(), node);

                baseTreeNodeList.add(node);
            }
            traverseList = notMatchedList;
        }
        return baseTreeNodeList;
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(ApiScenarioModuleRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extApiScenarioModuleMapper.selectIdAndParentIdByRequest(request);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get(UNPLANNED_SCENARIO));
    }

    public String add(ModuleCreateRequest request, String operator) {
        ApiScenarioModule module = new ApiScenarioModule();
        module.setId(IDGenerator.nextStr());
        module.setName(request.getName());
        module.setParentId(request.getParentId());
        module.setProjectId(request.getProjectId());
        module.setCreateUser(operator);
        this.checkDataValidity(module);
        module.setCreateTime(System.currentTimeMillis());
        module.setUpdateTime(module.getCreateTime());
        module.setPos(this.getNextOrder(request.getParentId()));
        module.setUpdateUser(operator);
        apiScenarioModuleMapper.insertSelective(module);
        //记录日志
        apiScenarioModuleLogService.saveAddLog(module, operator);
        return module.getId();
    }

    public Long getNextOrder(String parentId) {
        Long maxPos = extApiScenarioModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    /**
     * 检查数据的合法性
     */
    private void checkDataValidity(ApiScenarioModule module) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        if (!StringUtils.equals(module.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            example.createCriteria().andIdEqualTo(module.getParentId())
                    .andProjectIdEqualTo(module.getProjectId());
            if (apiScenarioModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();
        }
        example.createCriteria().andParentIdEqualTo(module.getParentId())
                .andNameEqualTo(module.getName()).andIdNotEqualTo(module.getId())
                .andProjectIdEqualTo(module.getProjectId());
        if (apiScenarioModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
    }

    private String getRootNodeId(ApiScenarioModule module) {
        if (StringUtils.equals(module.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            return module.getId();
        } else {
            ApiScenarioModule parentModule = apiScenarioModuleMapper.selectByPrimaryKey(module.getParentId());
            return this.getRootNodeId(parentModule);
        }
    }

    public void update(ModuleUpdateRequest request, String userId) {
        ApiScenarioModule module = checkResourceExist(request.getId());
        ApiScenarioModule updateModule = new ApiScenarioModule();
        updateModule.setId(request.getId());
        updateModule.setName(request.getName());
        updateModule.setParentId(module.getParentId());
        updateModule.setProjectId(module.getProjectId());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        apiScenarioModuleMapper.updateByPrimaryKeySelective(updateModule);
        //记录日志
        apiScenarioModuleLogService.saveUpdateLog(updateModule, userId);
    }


    public void deleteModule(String deleteId, String currentUser) {
        ApiScenarioModule deleteModule = checkResourceExist(deleteId);
        if (deleteModule != null) {
            deleteModule(List.of(deleteId), currentUser, deleteModule.getProjectId());
        }
    }

    public ApiScenarioModule checkResourceExist(String id) {
        ApiScenarioModule module = apiScenarioModuleMapper.selectByPrimaryKey(id);
        if (module == null) {
            throw new MSException(Translator.get(MODULE_NO_EXIST));
        }
        return module;
    }

    public void deleteModule(List<String> deleteIds, String currentUser, String projectId) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        List<BaseTreeNode> baseTreeNodes = extApiScenarioModuleMapper.selectNodeByIds(deleteIds);
        extApiScenarioModuleMapper.deleteByIds(deleteIds);
        //记录日志
        apiScenarioModuleLogService.saveDeleteModuleLog(baseTreeNodes, currentUser, projectId);
        batchDeleteData(deleteIds, currentUser, projectId);

        List<String> childrenIds = extApiScenarioModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds, currentUser, projectId);
        }
    }

    public void batchDeleteData(List<String> ids, String userId, String projectId) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andModuleIdIn(ids);
        long apiCount = apiScenarioMapper.countByExample(example);
        while (apiCount > 0) {
            PageHelper.startPage(1, 500);
            List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
            //提取id为新的集合
            List<String> refIds = apiScenarios.stream().map(ApiScenario::getRefId).distinct().toList();
            //删除场景
            extApiScenarioModuleMapper.deleteScenarioToGc(refIds, userId, System.currentTimeMillis());
            apiScenarioModuleLogService.saveDeleteDataLog(apiScenarios, userId, projectId);
            apiCount = apiScenarioMapper.countByExample(example);
        }
    }

    public void moveNode(NodeMoveRequest request, String currentUser) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                extApiScenarioModuleMapper::selectBaseModuleById,
                extApiScenarioModuleMapper::selectModuleByParentIdAndPosOperator);

        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andIdEqualTo(request.getDragNodeId());
        //节点换到了别的节点下,要先更新parent节点.
        if (apiScenarioModuleMapper.countByExample(example) == 0) {
            ApiScenarioModule definitionModule = new ApiScenarioModule();
            ApiScenarioModule currentModule = apiScenarioModuleMapper.selectByPrimaryKey(request.getDragNodeId());
            currentModule.setParentId(nodeSortDTO.getParent().getId());
            checkDataValidity(currentModule);
            definitionModule.setId(request.getDragNodeId());
            definitionModule.setParentId(nodeSortDTO.getParent().getId());
            apiScenarioModuleMapper.updateByPrimaryKeySelective(definitionModule);
        }

        super.sort(nodeSortDTO);
        //记录日志
        apiScenarioModuleLogService.saveMoveLog(nodeSortDTO, currentUser);
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     */
    public Map<String, Long> getModuleCountMap(ApiScenarioModuleRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(request, moduleCountDTOList);
        return super.getIdCountMapByBreadth(treeNodeList);
    }


    @Override
    public void updatePos(String id, long pos) {
        ApiScenarioModule updateModule = new ApiScenarioModule();
        updateModule.setPos(pos);
        updateModule.setId(id);
        apiScenarioModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = extApiScenarioModuleMapper.selectChildrenIdsSortByPos(parentId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioModuleMapper batchUpdateMapper = sqlSession.getMapper(ApiScenarioModuleMapper.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            ApiScenarioModule updateModule = new ApiScenarioModule();
            updateModule.setId(nodeId);
            updateModule.setPos((i + 1) * LIMIT_POS);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModule);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public Map<String, Long> moduleCount(ApiScenarioModuleRequest request, boolean deleted) {
        if (StringUtils.isNotEmpty(request.getTestPlanId())) {
            this.checkTestPlanRepeatCase(request);
        }
        request.setModuleIds(null);
        //查找根据moduleIds查找模块下的接口数量 查非delete状态的
        List<ModuleCountDTO> moduleCountDTOList = extApiScenarioMapper.countModuleIdByRequest(request, deleted);
        long allCount = getAllCount(moduleCountDTOList);
        request.setKeyword(null);
        request.setScenarioId(null);
        Map<String, Long> moduleCountMap = getModuleCountMap(request, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    private void checkTestPlanRepeatCase(ApiScenarioModuleRequest request) {
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(request.getTestPlanId());
        if (testPlanConfig != null && BooleanUtils.isTrue(testPlanConfig.getRepeatCase())) {
            //测试计划允许重复用例，意思就是统计不受测试计划影响。去掉这个条件，
            request.setTestPlanId(null);
        }
    }

    public List<BaseTreeNode> getTrashTree(ApiScenarioModuleRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andDeletedEqualTo(true);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiScenarios)) {
            return new ArrayList<>();
        }
        List<String> moduleIds = apiScenarios.stream().map(ApiScenario::getModuleId).distinct().toList();
        List<BaseTreeNode> baseTreeNodes = getNodeByNodeIds(moduleIds);
        return super.buildTreeAndCountResource(baseTreeNodes, true, Translator.get(UNPLANNED_SCENARIO));
    }

    public List<BaseTreeNode> getNodeByNodeIds(List<String> moduleIds) {
        List<String> finalModuleIds = new ArrayList<>(moduleIds);
        List<BaseTreeNode> totalList = new ArrayList<>();
        while (CollectionUtils.isNotEmpty(finalModuleIds)) {
            List<BaseTreeNode> modules = extApiScenarioModuleMapper.selectBaseByIds(finalModuleIds);
            totalList.addAll(modules);
            List<String> finalModuleIdList = finalModuleIds;
            List<String> parentModuleIds = modules.stream().map(BaseTreeNode::getParentId).filter(parentId -> !StringUtils.equalsIgnoreCase(parentId, ModuleConstants.ROOT_NODE_PARENT_ID) && !finalModuleIdList.contains(parentId)).toList();
            finalModuleIds.clear();
            finalModuleIds = new ArrayList<>(parentModuleIds);
        }
        return totalList.stream().distinct().toList();
    }
}
