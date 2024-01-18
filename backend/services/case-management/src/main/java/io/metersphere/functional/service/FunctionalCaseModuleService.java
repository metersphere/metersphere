/**
 * @filename:FunctionalCaseModuleServiceImpl 2023年5月17日
 * @project ms  V3.x
 * Copyright(c) 2018 wx Co. Ltd.
 * All right reserved.
 */
package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseExample;
import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.functional.domain.FunctionalCaseModuleExample;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseModuleMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseModuleMapper;
import io.metersphere.functional.request.FunctionalCaseModuleCreateRequest;
import io.metersphere.functional.request.FunctionalCaseModuleUpdateRequest;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.service.ModuleTreeService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseModuleService extends ModuleTreeService {
    @Resource
    private FunctionalCaseModuleMapper functionalCaseModuleMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtFunctionalCaseModuleMapper extFunctionalCaseModuleMapper;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private OperationLogService operationLogService;

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> functionalModuleList = extFunctionalCaseModuleMapper.selectBaseByProjectId(projectId);
        return super.buildTreeAndCountResource(functionalModuleList, true, Translator.get("default.module"));
    }
    
    public String add(FunctionalCaseModuleCreateRequest request, String userId) {
        FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
        functionalCaseModule.setId(IDGenerator.nextStr());
        functionalCaseModule.setName(request.getName());
        functionalCaseModule.setParentId(request.getParentId());
        functionalCaseModule.setProjectId(request.getProjectId());
        this.checkDataValidity(functionalCaseModule);
        functionalCaseModule.setCreateTime(System.currentTimeMillis());
        functionalCaseModule.setUpdateTime(functionalCaseModule.getCreateTime());
        functionalCaseModule.setPos(this.countPos(request.getParentId()));
        functionalCaseModule.setCreateUser(userId);
        functionalCaseModule.setUpdateUser(userId);
        functionalCaseModuleMapper.insert(functionalCaseModule);
        return functionalCaseModule.getId();
    }

    public void update(FunctionalCaseModuleUpdateRequest request, String userId) {
        FunctionalCaseModule updateModule = functionalCaseModuleMapper.selectByPrimaryKey(request.getId());
        if (updateModule == null) {
            throw new MSException(Translator.get("case_module.not.exist"));
        }
        updateModule.setName(request.getName());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        updateModule.setCreateUser(null);
        updateModule.setCreateTime(null);
        functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    public void moveNode(NodeMoveRequest request, String userId) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                extFunctionalCaseModuleMapper::selectBaseModuleById,
                extFunctionalCaseModuleMapper::selectModuleByParentIdAndPosOperator);

        FunctionalCaseModuleExample example = new FunctionalCaseModuleExample();
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andIdEqualTo(request.getDragNodeId());
        //节点换到了别的节点下,要先更新parent节点再计算sort
        if (functionalCaseModuleMapper.countByExample(example) == 0) {
            FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
            functionalCaseModule.setId(request.getDragNodeId());
            functionalCaseModule.setParentId(nodeSortDTO.getParent().getId());
            functionalCaseModule.setUpdateUser(userId);
            functionalCaseModule.setUpdateTime(System.currentTimeMillis());
            functionalCaseModuleMapper.updateByPrimaryKeySelective(functionalCaseModule);
        }
        super.sort(nodeSortDTO);
    }

    public void deleteModule(String moduleId) {
        FunctionalCaseModule deleteModule = functionalCaseModuleMapper.selectByPrimaryKey(moduleId);
        if (deleteModule != null) {
            List<FunctionalCase> functionalCases = this.deleteModuleByIds(Collections.singletonList(moduleId), new ArrayList<>());
            batchDelLog(functionalCases, deleteModule.getProjectId());
        }
    }

    public void batchDelLog(List<FunctionalCase> functionalCases, String projectId) {
        List<LogDTO> dtoList = new ArrayList<>();
        functionalCases.forEach(item -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.FUNCTIONAL_CASE,
                    item.getName());

            dto.setPath("/functional/case/module/delete/");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        operationLogService.batchAdd(dtoList);
    }

    public List<FunctionalCase> deleteModuleByIds(List<String>deleteIds,  List<FunctionalCase>functionalCases){
        if (CollectionUtils.isEmpty(deleteIds)) {
            return functionalCases;
        }
        FunctionalCaseModuleExample functionalCaseModuleExample = new FunctionalCaseModuleExample();
        functionalCaseModuleExample.createCriteria().andIdIn(deleteIds);
        functionalCaseModuleMapper.deleteByExample(functionalCaseModuleExample);
        List<FunctionalCase> functionalCaseList = extFunctionalCaseMapper.checkCaseByModuleIds(deleteIds);
        if (CollectionUtils.isNotEmpty(functionalCaseList)) {
            functionalCases.addAll(functionalCaseList);
        }
        extFunctionalCaseMapper.removeToTrashByModuleIds(deleteIds);
        List<String> childrenIds = extFunctionalCaseModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModuleByIds(childrenIds, functionalCases);
        }
        return functionalCases;
    }

    private Long countPos(String parentId) {
        Long maxPos = extFunctionalCaseModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     *
     */
    public Map<String, Long> getModuleCountMap(String projectId, List<ModuleCountDTO> moduleCountDTOList) {

        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(projectId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        return super.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extFunctionalCaseModuleMapper.selectIdAndParentIdByProjectId(projectId);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get("default.module"));
    }

    /**
     * 检查数据的合法性
     */
    private void checkDataValidity(FunctionalCaseModule functionalCaseModule) {
        FunctionalCaseModuleExample example = new FunctionalCaseModuleExample();
        if (!StringUtils.equals(functionalCaseModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //检查父ID是否存在
            example.createCriteria().andIdEqualTo(functionalCaseModule.getParentId());
            if (functionalCaseModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();

            //检查项目ID是否和父节点ID一致
            example.createCriteria().andProjectIdEqualTo(functionalCaseModule.getProjectId()).andIdEqualTo(functionalCaseModule.getParentId());
            if (functionalCaseModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("project.cannot.match.parent"));
            }
            example.clear();
        }
        example.createCriteria().andParentIdEqualTo(functionalCaseModule.getParentId()).andNameEqualTo(functionalCaseModule.getName()).andIdNotEqualTo(functionalCaseModule.getId());
        if (functionalCaseModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();

        //非默认节点，检查该节点所在分支的总长度，确保不超过阈值
        if (!StringUtils.equals(functionalCaseModule.getId(), ModuleConstants.DEFAULT_NODE_ID)) {
            this.checkBranchModules(this.getRootNodeId(functionalCaseModule), extFunctionalCaseModuleMapper::selectChildrenIdsByParentIds);
        }
    }

    private String getRootNodeId(FunctionalCaseModule functionalCaseModule) {
        if (StringUtils.equals(functionalCaseModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            return functionalCaseModule.getId();
        } else {
            FunctionalCaseModule parentModule = functionalCaseModuleMapper.selectByPrimaryKey(functionalCaseModule.getParentId());
            return this.getRootNodeId(parentModule);
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        FunctionalCaseModule updateModule = new FunctionalCaseModule();
        updateModule.setPos(pos);
        updateModule.setId(id);
        functionalCaseModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = extFunctionalCaseModuleMapper.selectChildrenIdsSortByPos(parentId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseModuleMapper batchUpdateMapper = sqlSession.getMapper(FunctionalCaseModuleMapper.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            FunctionalCaseModule updateModule = new FunctionalCaseModule();
            updateModule.setId(nodeId);
            updateModule.setPos((i + 1) * LIMIT_POS);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModule);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public List<BaseTreeNode> getTrashTree(String projectId) {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andDeletedEqualTo(true).andProjectIdEqualTo(projectId);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        if (CollectionUtils.isEmpty(functionalCases)) {
            return new ArrayList<>();
        }
        List<String> moduleIds = functionalCases.stream().map(FunctionalCase::getModuleId).distinct().toList();
        List<BaseTreeNode> nodeByNodeIds = getNodeByNodeIds(moduleIds);
        return super.buildTreeAndCountResource(nodeByNodeIds, true, Translator.get("default.module"));
    }

    public List<BaseTreeNode> getNodeByNodeIds(List<String>moduleIds){
        List<String> finalModuleIds = new ArrayList<>(moduleIds);
        List<BaseTreeNode> totalList = new ArrayList<>();
        while (CollectionUtils.isNotEmpty(finalModuleIds))  {
            List<BaseTreeNode> modules = extFunctionalCaseModuleMapper.selectBaseByIds(finalModuleIds);
            totalList.addAll(modules);
            List<String> parentModuleIds = modules.stream().map(BaseTreeNode::getParentId).filter(parentId -> !StringUtils.equalsIgnoreCase(parentId,ModuleConstants.ROOT_NODE_PARENT_ID)).toList();
            finalModuleIds.clear();
            finalModuleIds = parentModuleIds;
        }
        return totalList.stream().distinct().toList();
    }

}