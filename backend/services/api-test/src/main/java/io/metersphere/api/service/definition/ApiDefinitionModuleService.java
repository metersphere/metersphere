package io.metersphere.api.service.definition;

import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiTreeNode;
import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.debug.ModuleUpdateRequest;
import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.debug.ApiDebugModuleService;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionModuleService extends ModuleTreeService {
    private static final String UNPLANNED_API = "api_unplanned_request";
    private static final String MODULE_NO_EXIST = "api_module.not.exist";
    private static final String DEBUG_MODULE_COUNT_ALL = "all";
    @Resource
    private ApiDefinitionModuleLogService apiDefinitionModuleLogService;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ExtApiDefinitionModuleMapper extApiDefinitionModuleMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiDebugModuleService apiDebugModuleService;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiTestCaseLogService apiTestCaseLogService;

    public List<BaseTreeNode> getTree(ApiModuleRequest request) {
        //接口的树结构是  模块：子模块+接口 接口为非delete状态的
        List<BaseTreeNode> fileModuleList = extApiDefinitionModuleMapper.selectBaseByRequest(request);
        List<BaseTreeNode> baseTreeNodes = super.buildTreeAndCountResource(fileModuleList, true, Translator.get(UNPLANNED_API));
        List<ApiTreeNode> apiTreeNodeList = extApiDefinitionModuleMapper.selectApiDataByRequest(request);
        return apiDebugModuleService.getBaseTreeNodes(apiTreeNodeList, baseTreeNodes);

    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(ApiModuleRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extApiDefinitionModuleMapper.selectIdAndParentIdByRequest(request);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get(UNPLANNED_API));
    }

    public String add(ModuleCreateRequest request, String operator) {
        ApiDefinitionModule module = new ApiDefinitionModule();
        module.setId(IDGenerator.nextStr());
        module.setName(request.getName());
        module.setParentId(request.getParentId());
        module.setProjectId(request.getProjectId());
        module.setProtocol(request.getProtocol());
        module.setCreateUser(operator);
        this.checkDataValidity(module);
        module.setCreateTime(System.currentTimeMillis());
        module.setUpdateTime(module.getCreateTime());
        module.setPos(this.countPos(request.getParentId()));
        module.setUpdateUser(operator);
        apiDefinitionModuleMapper.insert(module);
        //记录日志
        apiDefinitionModuleLogService.saveAddLog(module, operator);
        return module.getId();
    }

    private Long countPos(String parentId) {
        Long maxPos = extApiDefinitionModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    /**
     * 检查数据的合法性
     */
    private void checkDataValidity(ApiDefinitionModule module) {
        ApiDefinitionModuleExample example = new ApiDefinitionModuleExample();
        if (!StringUtils.equals(module.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //检查父ID是否存在  接口模块的逻辑是  同一个协议下的  同一个项目的同层级节点下不能有相同的名称
            example.createCriteria().andIdEqualTo(module.getParentId())
                    .andProtocolEqualTo(module.getProtocol())
                    .andProjectIdEqualTo(module.getProjectId());
            if (apiDefinitionModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();
        }
        example.createCriteria().andParentIdEqualTo(module.getParentId())
                .andNameEqualTo(module.getName()).andIdNotEqualTo(module.getId())
                .andProtocolEqualTo(module.getProtocol()).andProjectIdEqualTo(module.getProjectId());
        if (apiDefinitionModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
    }

    public void update(ModuleUpdateRequest request, String userId) {
        ApiDefinitionModule module = apiDefinitionModuleMapper.selectByPrimaryKey(request.getId());
        if (module == null) {
            throw new MSException(Translator.get(MODULE_NO_EXIST));
        }
        ApiDefinitionModule updateModule = new ApiDefinitionModule();
        updateModule.setId(request.getId());
        updateModule.setName(request.getName());
        updateModule.setParentId(module.getParentId());
        updateModule.setProjectId(module.getProjectId());
        updateModule.setProtocol(module.getProtocol());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        apiDefinitionModuleMapper.updateByPrimaryKeySelective(updateModule);
        //记录日志
        apiDefinitionModuleLogService.saveUpdateLog(updateModule, userId);
    }


    public void deleteModule(String deleteId, String currentUser) {
        ApiDefinitionModule deleteModule = apiDefinitionModuleMapper.selectByPrimaryKey(deleteId);
        if (deleteModule != null) {
            deleteModule(List.of(deleteId), currentUser, deleteModule.getProjectId());
        }
    }

    public void deleteModule(List<String> deleteIds, String currentUser, String projectId) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        List<BaseTreeNode> baseTreeNodes = extApiDefinitionModuleMapper.selectNodeByIds(deleteIds);
        extApiDefinitionModuleMapper.deleteByIds(deleteIds);
        //记录日志
        apiDefinitionModuleLogService.saveDeleteModuleLog(baseTreeNodes, currentUser, projectId);
        //删除模块下的所有接口  TODO  需要掉接口那边的删除方法 分批删除  还需要删除接口下面关联的case数据
        batchDeleteData(deleteIds, currentUser, projectId);

        List<String> childrenIds = extApiDefinitionModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds, currentUser, projectId);
        }
    }

    public void batchDeleteData(List<String> ids, String userId, String projectId) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andModuleIdIn(ids).andDeletedEqualTo(false);
        long apiCount = apiDefinitionMapper.countByExample(example);
        while (apiCount > 0) {
            PageHelper.startPage(1, 500);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            //提取id为新的集合
            List<String> refIds = apiDefinitions.stream().map(ApiDefinition::getRefId).distinct().toList();
            //删除接口
            extApiDefinitionMapper.deleteApiToGc(refIds, userId, System.currentTimeMillis());
            apiDefinitionModuleLogService.saveDeleteDataLog(apiDefinitions, userId, projectId);
            //删除接口用例
            List<String> apiIds = apiDefinitions.stream().map(ApiDefinition::getId).distinct().toList();
            List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(apiIds, false);
            List<String> caseIds = caseLists.stream().map(ApiTestCase::getId).distinct().toList();
            apiTestCaseService.batchDeleteToGc(caseIds, userId, projectId, false);
            apiDefinitionModuleLogService.saveDeleteCaseLog(caseLists, userId, projectId);
            apiCount = apiDefinitionMapper.countByExample(example);
        }
    }

    public void moveNode(NodeMoveRequest request, String currentUser) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                extApiDefinitionModuleMapper::selectBaseModuleById,
                extApiDefinitionModuleMapper::selectModuleByParentIdAndPosOperator);

        ApiDefinitionModuleExample example = new ApiDefinitionModuleExample();
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andIdEqualTo(request.getDragNodeId());
        //节点换到了别的节点下,要先更新parent节点.
        if (apiDefinitionModuleMapper.countByExample(example) == 0) {
            ApiDefinitionModule definitionModule = new ApiDefinitionModule();
            ApiDefinitionModule currentModule = apiDefinitionModuleMapper.selectByPrimaryKey(request.getDragNodeId());
            currentModule.setParentId(nodeSortDTO.getParent().getId());
            checkDataValidity(currentModule);
            definitionModule.setId(request.getDragNodeId());
            definitionModule.setParentId(nodeSortDTO.getParent().getId());
            apiDefinitionModuleMapper.updateByPrimaryKeySelective(definitionModule);
        }

        super.sort(nodeSortDTO);
        //记录日志
        apiDefinitionModuleLogService.saveMoveLog(nodeSortDTO, currentUser);
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     */
    public Map<String, Long> getModuleCountMap(ApiModuleRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(request, moduleCountDTOList);
        return super.getIdCountMapByBreadth(treeNodeList);
    }


    @Override
    public void updatePos(String id, long pos) {
        ApiDefinitionModule updateModule = new ApiDefinitionModule();
        updateModule.setPos(pos);
        updateModule.setId(id);
        apiDefinitionModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = extApiDefinitionModuleMapper.selectChildrenIdsSortByPos(parentId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionModuleMapper batchUpdateMapper = sqlSession.getMapper(ApiDefinitionModuleMapper.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            ApiDefinitionModule updateModule = new ApiDefinitionModule();
            updateModule.setId(nodeId);
            updateModule.setPos((i + 1) * LIMIT_POS);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModule);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public Map<String, Long> moduleCount(ApiModuleRequest request) {
        request.setModuleIds(null);
        //查找根据moduleIds查找模块下的接口数量 查非delete状态的
        List<ModuleCountDTO> moduleCountDTOList = extApiDefinitionModuleMapper.countModuleIdByRequest(request);
        long allCount = getAllCount(moduleCountDTOList);
        Map<String, Long> moduleCountMap = getModuleCountMap(request, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

}
