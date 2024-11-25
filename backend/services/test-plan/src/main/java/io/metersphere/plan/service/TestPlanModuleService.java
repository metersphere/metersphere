package io.metersphere.plan.service;

import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanModuleCreateRequest;
import io.metersphere.plan.dto.request.TestPlanModuleUpdateRequest;
import io.metersphere.plan.mapper.ExtTestPlanModuleMapper;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.service.ModuleTreeService;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestPlanModule;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.mapper.TestPlanModuleMapper;
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
public class TestPlanModuleService extends ModuleTreeService {
    @Resource
    private TestPlanModuleMapper testPlanModuleMapper;
    @Resource
    private ExtTestPlanModuleMapper extTestPlanModuleMapper;
    @Resource
    protected SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanModuleLogService testPlanModuleLogService;
    @Resource
    private TestPlanService testPlanService;

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> fileModuleList = extTestPlanModuleMapper.selectBaseByProjectId(projectId);
        return super.buildTreeAndCountResource(fileModuleList, true, Translator.get("unplanned.plan"));
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extTestPlanModuleMapper.selectIdAndParentIdByProjectId(projectId);
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get("default.module"));
    }

    public String add(TestPlanModuleCreateRequest request, String operator, String requestUrl, String requestMethod) {
        TestPlanModule testPlanModule = new TestPlanModule();
        testPlanModule.setId(IDGenerator.nextStr());
        testPlanModule.setName(request.getName());
        testPlanModule.setParentId(request.getParentId());
        testPlanModule.setProjectId(request.getProjectId());
        this.checkDataValidity(testPlanModule);
        testPlanModule.setCreateTime(System.currentTimeMillis());
        testPlanModule.setUpdateTime(testPlanModule.getCreateTime());
        testPlanModule.setPos(this.countPos(request.getParentId()));
        testPlanModule.setCreateUser(operator);
        testPlanModule.setUpdateUser(operator);
        testPlanModuleMapper.insert(testPlanModule);
        //记录日志
        testPlanModuleLogService.saveAddLog(testPlanModule, operator, requestUrl, requestMethod);
        return testPlanModule.getId();
    }

    protected Long countPos(String parentId) {
        Long maxPos = extTestPlanModuleMapper.getMaxPosByParentId(parentId);
        if (maxPos == null) {
            return LIMIT_POS;
        } else {
            return maxPos + LIMIT_POS;
        }
    }

    /**
     * 检查数据的合法性
     */
    protected void checkDataValidity(TestPlanModule module) {
        TestPlanModuleExample example = new TestPlanModuleExample();
        if (!StringUtils.equalsIgnoreCase(module.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
            //检查父ID是否存在
            example.createCriteria().andIdEqualTo(module.getParentId());
            if (testPlanModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("parent.node.not_blank"));
            }
            example.clear();
            if (StringUtils.isNotBlank(module.getProjectId())) {
                //检查项目ID是否和父节点ID一致
                example.createCriteria().andProjectIdEqualTo(module.getProjectId()).andIdEqualTo(module.getParentId());
                if (testPlanModuleMapper.countByExample(example) == 0) {
                    throw new MSException(Translator.get("project.cannot.match.parent"));
                }
                example.clear();
            }
        }
        example.createCriteria().andParentIdEqualTo(module.getParentId()).andProjectIdEqualTo(module.getProjectId()).andNameEqualTo(module.getName()).andIdNotEqualTo(module.getId());
        if (testPlanModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("node.name.repeat"));
        }
        example.clear();
    }

    public void update(TestPlanModuleUpdateRequest request, String userId, String requestUrl, String requestMethod) {
        TestPlanModule module = testPlanModuleMapper.selectByPrimaryKey(request.getId());
        TestPlanModule updateModule = new TestPlanModule();
        updateModule.setId(request.getId());
        updateModule.setName(request.getName().trim());
        updateModule.setParentId(module.getParentId());
        updateModule.setProjectId(module.getProjectId());
        this.checkDataValidity(updateModule);
        updateModule.setUpdateTime(System.currentTimeMillis());
        updateModule.setUpdateUser(userId);
        testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);
        TestPlanModule newModule = testPlanModuleMapper.selectByPrimaryKey(request.getId());
        //记录日志
        testPlanModuleLogService.saveUpdateLog(module, newModule, module.getProjectId(), userId, requestUrl, requestMethod);
    }


    public void deleteModule(String deleteId, String operator, String requestUrl, String requestMethod) {
        TestPlanModule deleteModule = testPlanModuleMapper.selectByPrimaryKey(deleteId);
        if (deleteModule != null) {
            this.deleteModule(Collections.singletonList(deleteId), deleteModule.getProjectId(), operator, requestUrl, requestMethod);
            //记录日志
            testPlanModuleLogService.saveDeleteLog(deleteModule, operator, requestUrl, requestMethod);
        }
    }

    public void deleteModule(List<String> deleteIds, String projectId, String operator, String requestUrl, String requestMethod) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        extTestPlanModuleMapper.deleteByIds(deleteIds);
        List<String> planDeleteIds = extTestPlanModuleMapper.selectPlanIdsByModuleIds(deleteIds);
        if (CollectionUtils.isNotEmpty(planDeleteIds)) {
            TestPlanBatchProcessRequest request = new TestPlanBatchProcessRequest();
            request.setModuleIds(deleteIds);
            request.setSelectAll(false);
            request.setProjectId(projectId);
            request.setSelectIds(planDeleteIds);
            testPlanService.batchDelete(request, operator, requestUrl, requestMethod);
        }

        List<String> childrenIds = extTestPlanModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModule(childrenIds, projectId, operator, requestUrl, requestMethod);
        }
    }

    public void moveNode(NodeMoveRequest request, String currentUser, String requestUrl, String requestMethod) {

        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                extTestPlanModuleMapper::selectBaseModuleById,
                extTestPlanModuleMapper::selectModuleByParentIdAndPosOperator);

        TestPlanModuleExample example = new TestPlanModuleExample();
        // 拖拽后, 父级模块下存在同名模块
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andNameEqualTo(nodeSortDTO.getNode().getName())
                .andProjectIdEqualTo(nodeSortDTO.getNode().getProjectId())
                .andIdNotEqualTo(nodeSortDTO.getNode().getId());
        if (testPlanModuleMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("test_plan_module_already_exists"));
        }
        example.clear();
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andIdEqualTo(request.getDragNodeId());
        //节点换到了别的节点下,要先更新parent节点再计算sort
        if (testPlanModuleMapper.countByExample(example) == 0) {
            TestPlanModule module = new TestPlanModule();
            module.setId(request.getDragNodeId());
            module.setParentId(nodeSortDTO.getParent().getId());
            testPlanModuleMapper.updateByPrimaryKeySelective(module);
        }
        super.sort(nodeSortDTO);
        //记录日志
        testPlanModuleLogService.saveMoveLog(nodeSortDTO, currentUser, requestUrl, requestMethod);
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


    @Override
    public void updatePos(String id, long pos) {
        TestPlanModule updateModule = new TestPlanModule();
        updateModule.setPos(pos);
        updateModule.setId(id);
        testPlanModuleMapper.updateByPrimaryKeySelective(updateModule);
    }

    @Override
    public void refreshPos(String parentId) {
        List<String> childrenIdSortByPos = extTestPlanModuleMapper.selectChildrenIdsSortByPos(parentId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanModuleMapper batchUpdateMapper = sqlSession.getMapper(TestPlanModuleMapper.class);
        for (int i = 0; i < childrenIdSortByPos.size(); i++) {
            String nodeId = childrenIdSortByPos.get(i);
            TestPlanModule updateModule = new TestPlanModule();
            updateModule.setId(nodeId);
            updateModule.setPos((i + 1) * LIMIT_POS);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModule);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public String getNameById(String id) {
        return extTestPlanModuleMapper.selectNameById(id);
    }


    public List<BaseTreeNode> getNodeByNodeIds(List<String> moduleIds) {
        List<String> finalModuleIds = new ArrayList<>(moduleIds);
        List<BaseTreeNode> totalList = new ArrayList<>();
        while (CollectionUtils.isNotEmpty(finalModuleIds)) {
            List<BaseTreeNode> modules = extTestPlanModuleMapper.selectBaseByIds(finalModuleIds);
            totalList.addAll(modules);
            List<String> finalModuleIdList = finalModuleIds;
            List<String> parentModuleIds = modules.stream().map(BaseTreeNode::getParentId).filter(parentId -> !StringUtils.equalsIgnoreCase(parentId, ModuleConstants.ROOT_NODE_PARENT_ID) && !finalModuleIdList.contains(parentId)).toList();
            finalModuleIds.clear();
            finalModuleIds = new ArrayList<>(parentModuleIds);
        }
        return totalList.stream().distinct().toList();
    }
}
