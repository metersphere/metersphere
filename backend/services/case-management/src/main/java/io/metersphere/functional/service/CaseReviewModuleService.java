/**
 * @filename:CaseReviewModuleServiceImpl 2023年5月17日
 * @project ms  V3.x
 * Copyright(c) 2018 wx Co. Ltd.
 * All right reserved.
 */
package io.metersphere.functional.service;


import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.CaseReviewModule;
import io.metersphere.functional.domain.CaseReviewModuleExample;
import io.metersphere.functional.mapper.CaseReviewModuleMapper;
import io.metersphere.functional.mapper.ExtCaseReviewMapper;
import io.metersphere.functional.mapper.ExtCaseReviewModuleMapper;
import io.metersphere.functional.request.CaseReviewModuleCreateRequest;
import io.metersphere.functional.request.CaseReviewModuleUpdateRequest;
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
public class CaseReviewModuleService extends ModuleTreeService {
    @Resource
    private CaseReviewModuleMapper caseReviewModuleMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtCaseReviewModuleMapper extCaseReviewModuleMapper;
    @Resource
    private ExtCaseReviewMapper extCaseReviewMapper;
    @Resource
    private DeleteCaseReviewService deleteCaseReviewService;


    @Resource
    private OperationLogService operationLogService;

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> fileModuleList = extCaseReviewModuleMapper.selectBaseByProjectId(projectId);
        return super.buildTreeAndCountResource(fileModuleList, true, Translator.get("default.module"));
    }

    public String add(CaseReviewModuleCreateRequest request, String userId) {
        CaseReviewModule caseReviewModule = new CaseReviewModule();
        caseReviewModule.setId(IDGenerator.nextStr());
        caseReviewModule.setName(request.getName());
        caseReviewModule.setParentId(request.getParentId());
        caseReviewModule.setProjectId(request.getProjectId());
        this.checkDataValidity(caseReviewModule);
        caseReviewModule.setCreateTime(System.currentTimeMillis());
        caseReviewModule.setUpdateTime(caseReviewModule.getCreateTime());
        caseReviewModule.setPos(this.countPos(request.getParentId()));
        caseReviewModule.setCreateUser(userId);
        caseReviewModule.setUpdateUser(userId);
        caseReviewModuleMapper.insert(caseReviewModule);
        return caseReviewModule.getId();
    }

    public void update(CaseReviewModuleUpdateRequest request, String userId) {
        CaseReviewModule updateModule = caseReviewModuleMapper.selectByPrimaryKey(request.getId());
        if (updateModule == null) {
            throw new MSException(Translator.get("case_module.not.exist"));
        }
        updateModule.setName(request.getName());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        updateModule.setCreateUser(null);
        updateModule.setCreateTime(null);
        caseReviewModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    public void moveNode(NodeMoveRequest request, String userId) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                extCaseReviewModuleMapper::selectBaseModuleById,
                extCaseReviewModuleMapper::selectModuleByParentIdAndPosOperator);

        CaseReviewModuleExample example = new CaseReviewModuleExample();
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andIdEqualTo(request.getDragNodeId());
        //节点换到了别的节点下,要先更新parent节点再计算sort
        if (caseReviewModuleMapper.countByExample(example) == 0) {
            CaseReviewModule caseReviewModule = new CaseReviewModule();
            caseReviewModule.setId(request.getDragNodeId());
            caseReviewModule.setParentId(nodeSortDTO.getParent().getId());
            caseReviewModule.setUpdateUser(userId);
            caseReviewModule.setUpdateTime(System.currentTimeMillis());
            caseReviewModuleMapper.updateByPrimaryKeySelective(caseReviewModule);
        }
        super.sort(nodeSortDTO);
    }

    public void deleteModule(String moduleId) {
        CaseReviewModule deleteModule = caseReviewModuleMapper.selectByPrimaryKey(moduleId);
        if (deleteModule != null) {
            List<CaseReview> caseReviews = this.deleteModuleByIds(Collections.singletonList(moduleId), new ArrayList<>(), deleteModule.getProjectId());
            batchDelLog(caseReviews, deleteModule.getProjectId());
        }
    }

    public void batchDelLog(List<CaseReview> caseReviews, String projectId) {
        List<LogDTO> dtoList = new ArrayList<>();
        caseReviews.forEach(item -> {
            LogDTO dto = new LogDTO(
                    projectId,
                    "",
                    item.getId(),
                    item.getCreateUser(),
                    OperationLogType.DELETE.name(),
                    OperationLogModule.CASE_REVIEW,
                    item.getName());

            dto.setPath("/case/review/module/delete/");
            dto.setMethod(HttpMethodConstants.GET.name());
            dto.setOriginalValue(JSON.toJSONBytes(item));
            dtoList.add(dto);
        });
        operationLogService.batchAdd(dtoList);
    }

    public List<CaseReview> deleteModuleByIds(List<String> deleteIds, List<CaseReview> caseReviews, String projectId) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return caseReviews;
        }
        CaseReviewModuleExample caseReviewModuleExample = new CaseReviewModuleExample();
        caseReviewModuleExample.createCriteria().andIdIn(deleteIds);
        caseReviewModuleMapper.deleteByExample(caseReviewModuleExample);
        List<CaseReview> caseReviewList = extCaseReviewMapper.checkCaseByModuleIds(deleteIds);
        if (CollectionUtils.isNotEmpty(caseReviewList)) {
            caseReviews.addAll(caseReviewList);
        }
        deleteCaseReviewService.deleteCaseReviewResource(deleteIds, projectId, false);
        List<String> childrenIds = extCaseReviewModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModuleByIds(childrenIds, caseReviews, projectId);
        }
        return caseReviews;
    }

    private Long countPos(String parentId) {
        Long maxPos = extCaseReviewModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     */
    public Map<String, Long> getModuleCountMap(String projectId, List<ModuleCountDTO> moduleCountDTOList) {

        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(projectId, moduleCountDTOList);
        //通过广度遍历的方式构建返回值
        return super.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extCaseReviewModuleMapper.selectIdAndParentIdByProjectId(projectId);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get("default.module"));
    }

    /**
     * 检查数据的合法性
     */
    private void checkDataValidity(CaseReviewModule caseReviewModule) {
        CaseReviewModuleExample example = new CaseReviewModuleExample();
        if (!StringUtils.equals(caseReviewModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //检查父ID是否存在
            example.createCriteria().andIdEqualTo(caseReviewModule.getParentId());
            if (caseReviewModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();

            //检查项目ID是否和父节点ID一致
            example.createCriteria().andProjectIdEqualTo(caseReviewModule.getProjectId()).andIdEqualTo(caseReviewModule.getParentId());
            if (caseReviewModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("project.cannot.match.parent"));
            }
            example.clear();
        }
        example.createCriteria().andParentIdEqualTo(caseReviewModule.getParentId()).andNameEqualTo(caseReviewModule.getName()).andIdNotEqualTo(caseReviewModule.getId());
        if (caseReviewModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
    }

    private String getRootNodeId(CaseReviewModule caseReviewModule) {
        if (StringUtils.equals(caseReviewModule.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            return caseReviewModule.getId();
        } else {
            CaseReviewModule parentModule = caseReviewModuleMapper.selectByPrimaryKey(caseReviewModule.getParentId());
            return this.getRootNodeId(parentModule);
        }
    }

    @Override
    public void updatePos(String id, long pos) {
        CaseReviewModule updateModule = new CaseReviewModule();
        updateModule.setPos(pos);
        updateModule.setId(id);
        caseReviewModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = extCaseReviewModuleMapper.selectChildrenIdsSortByPos(parentId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CaseReviewModuleMapper batchUpdateMapper = sqlSession.getMapper(CaseReviewModuleMapper.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            CaseReviewModule updateModule = new CaseReviewModule();
            updateModule.setId(nodeId);
            updateModule.setPos((i + 1) * LIMIT_POS);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModule);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

}