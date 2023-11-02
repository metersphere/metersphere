/**
 * @filename:FunctionalCaseModuleServiceImpl 2023年5月17日
 * @project ms  V3.x
 * Copyright(c) 2018 wx Co. Ltd.
 * All right reserved.
 */
package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.functional.domain.FunctionalCaseModuleExample;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseModuleMapper;
import io.metersphere.functional.mapper.FunctionalCaseModuleMapper;
import io.metersphere.functional.request.FunctionalCaseModuleCreateRequest;
import io.metersphere.functional.request.FunctionalCaseModuleUpdateRequest;
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

import java.util.Collections;
import java.util.List;


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

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> fileModuleList = extFunctionalCaseModuleMapper.selectBaseByProjectId(projectId);
        return super.buildTreeAndCountResource(fileModuleList, true, Translator.get("default.module"));
    }
    
    public void add(FunctionalCaseModuleCreateRequest request, String userId) {
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
            this.deleteModuleByIds(Collections.singletonList(moduleId));
        }
    }

    public void deleteModuleByIds(List<String>deleteIds){
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        FunctionalCaseModuleExample functionalCaseModuleExample = new FunctionalCaseModuleExample();
        functionalCaseModuleExample.createCriteria().andIdIn(deleteIds);
        functionalCaseModuleMapper.deleteByExample(functionalCaseModuleExample);
        extFunctionalCaseMapper.removeToTrashByModuleIds(deleteIds);
        List<String> childrenIds = extFunctionalCaseModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModuleByIds(childrenIds);
        }
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

            if (StringUtils.isNotBlank(functionalCaseModule.getProjectId())) {
                //检查项目ID是否和父节点ID一致
                example.createCriteria().andProjectIdEqualTo(functionalCaseModule.getProjectId()).andIdEqualTo(functionalCaseModule.getParentId());
                if (functionalCaseModuleMapper.countByExample(example) == 0) {
                    throw new MSException(Translator.get("project.cannot.match.parent"));
                }
                example.clear();
            }
        }
        example.createCriteria().andParentIdEqualTo(functionalCaseModule.getParentId()).andNameEqualTo(functionalCaseModule.getName()).andIdNotEqualTo(functionalCaseModule.getId());
        if (functionalCaseModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
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



}