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
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
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
import java.util.concurrent.atomic.AtomicReference;


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
    private UserMapper userMapper;
    @Resource
    private FunctionalCaseNoticeService functionalCaseNoticeService;
    @Resource
    private FunctionalCaseModuleLogService functionalCaseModuleLogService;


    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> functionalModuleList = extFunctionalCaseModuleMapper.selectBaseByProjectId(projectId);
        functionalModuleList.forEach(baseTreeNode -> {
            if (StringUtils.equalsIgnoreCase(baseTreeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                baseTreeNode.setPath(baseTreeNode.getPath() + baseTreeNode.getName());
            }
        });
        return super.buildTreeAndCountResource(functionalModuleList, true, Translator.get("functional_case.module.default.name"));
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
        functionalCaseModuleLogService.addModuleLog(functionalCaseModule, userId);
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
        functionalCaseModuleLogService.updateModuleLog(updateModule, userId);
    }

    public void moveNode(NodeMoveRequest request, String userId) {
        NodeSortDTO nodeSortDTO = super.getNodeSortDTO(request,
                extFunctionalCaseModuleMapper::selectBaseModuleById,
                extFunctionalCaseModuleMapper::selectModuleByParentIdAndPosOperator);

        FunctionalCaseModuleExample example = new FunctionalCaseModuleExample();
        example.createCriteria().andParentIdEqualTo(nodeSortDTO.getParent().getId()).andIdEqualTo(request.getDragNodeId());
        //节点换到了别的节点下,要先更新parent节点再计算sort
        if (functionalCaseModuleMapper.countByExample(example) == 0) {
            FunctionalCaseModule moveModule = functionalCaseModuleMapper.selectByPrimaryKey(request.getDragNodeId());
            moveModule.setParentId(nodeSortDTO.getParent().getId());
            this.checkDataValidity(moveModule);

            FunctionalCaseModule functionalCaseModule = new FunctionalCaseModule();
            functionalCaseModule.setId(request.getDragNodeId());
            functionalCaseModule.setParentId(nodeSortDTO.getParent().getId());
            functionalCaseModule.setUpdateUser(userId);
            functionalCaseModule.setUpdateTime(System.currentTimeMillis());
            functionalCaseModuleMapper.updateByPrimaryKeySelective(functionalCaseModule);
        }
        super.sort(nodeSortDTO);
    }

    public void deleteModule(String moduleId, String userId) {
        FunctionalCaseModule deleteModule = functionalCaseModuleMapper.selectByPrimaryKey(moduleId);
        if (deleteModule != null) {
            List<FunctionalCase> functionalCases = this.deleteModuleByIds(Collections.singletonList(moduleId), new ArrayList<>(), userId);
            //用例日志
            functionalCaseModuleLogService.batchDelLog(functionalCases, deleteModule.getProjectId(), userId, "/functional/case/module/delete/" + moduleId);
            //模块日志
            functionalCaseModuleLogService.handleModuleLog(List.of(deleteModule), deleteModule.getProjectId(), userId, "/functional/case/module/delete/" + moduleId, OperationLogType.DELETE.name(), " " + Translator.get("log.delete_module"));
            List<String> ids = functionalCases.stream().map(FunctionalCase::getId).toList();
            User user = userMapper.selectByPrimaryKey(userId);
            functionalCaseNoticeService.batchSendNotice(deleteModule.getProjectId(), ids, user, NoticeConstants.Event.DELETE);
        }
    }

    public List<FunctionalCase> deleteModuleByIds(List<String> deleteIds, List<FunctionalCase> functionalCases, String userId) {
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
        extFunctionalCaseMapper.removeToTrashByModuleIds(deleteIds, userId);
        List<String> childrenIds = extFunctionalCaseModuleMapper.selectChildrenIdsByParentIds(deleteIds);
        if (CollectionUtils.isNotEmpty(childrenIds)) {
            deleteModuleByIds(childrenIds, functionalCases, userId);
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
        return super.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get("functional_case.module.default.name"));
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
        example.createCriteria().andParentIdEqualTo(functionalCaseModule.getParentId()).andNameEqualTo(functionalCaseModule.getName()).andIdNotEqualTo(functionalCaseModule.getId()).andProjectIdEqualTo(functionalCaseModule.getProjectId());
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

    public List<BaseTreeNode> getTrashTree(String projectId) {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andDeletedEqualTo(true).andProjectIdEqualTo(projectId);
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        if (CollectionUtils.isEmpty(functionalCases)) {
            return new ArrayList<>();
        }
        List<String> moduleIds = functionalCases.stream().map(FunctionalCase::getModuleId).distinct().toList();
        List<BaseTreeNode> nodeByNodeIds = getNodeByNodeIds(moduleIds);
        return super.buildTreeAndCountResource(nodeByNodeIds, true, Translator.get("functional_case.module.default.name"));
    }

    public List<BaseTreeNode> getNodeByNodeIds(List<String> moduleIds) {
        List<String> finalModuleIds = new ArrayList<>(moduleIds);
        List<BaseTreeNode> totalList = new ArrayList<>();
        while (CollectionUtils.isNotEmpty(finalModuleIds)) {
            List<BaseTreeNode> modules = extFunctionalCaseModuleMapper.selectBaseByIds(finalModuleIds);
            totalList.addAll(modules);
            List<String> finalModuleIdList = finalModuleIds;
            List<String> parentModuleIds = modules.stream().map(BaseTreeNode::getParentId).filter(parentId -> !StringUtils.equalsIgnoreCase(parentId, ModuleConstants.ROOT_NODE_PARENT_ID) && !finalModuleIdList.contains(parentId)).toList();
            finalModuleIds.clear();
            finalModuleIds = new ArrayList<>(parentModuleIds);
        }
        return totalList.stream().distinct().toList();
    }


    /**
     * 根据模块路径创建模块
     *
     * @param modulePath 模块路径
     * @param projectId  项目ID
     * @param moduleTree 已存在的模块树
     * @param userId     userId
     */
    public Map<String, String> createCaseModule(List<String> modulePath, String projectId, List<BaseTreeNode> moduleTree, String userId, Map<String, String> pathMap) {
        modulePath.forEach(path -> {
            List<String> moduleNames = new ArrayList<>(List.of(path.split("/")));
            Iterator<String> itemIterator = moduleNames.iterator();
            AtomicReference<Boolean> hasNode = new AtomicReference<>(false);
            //当前节点模块名称
            String currentModuleName;
            if (moduleNames.size() <= 1) {
                throw new MSException(Translator.get("test_case_create_module_fail") + ":" + path);
            } else {
                itemIterator.next();
                itemIterator.remove();
                currentModuleName = itemIterator.next().trim();
                moduleTree.forEach(module -> {
                    //根节点是否存在
                    if (StringUtils.equalsIgnoreCase(currentModuleName, module.getName())) {
                        hasNode.set(true);
                        //根节点存在，检查子节点是否存在
                        createModuleByPathIterator(itemIterator, "/" + currentModuleName, module, pathMap, projectId, userId);
                    }
                });
            }
            if (!hasNode.get()) {
                //根节点不存在，直接创建
                createModuleByPath(itemIterator, currentModuleName, null, projectId, StringUtils.EMPTY, pathMap, userId);
            }
        });
        return pathMap;
    }


    /**
     * 根据模块路径迭代器遍历模块路径
     *
     * @param itemIterator      模块路径迭代器
     * @param currentModulePath 当前节点路径： /模块1/模块2
     * @param module            当前模块对象
     * @param pathMap           记录新创建的模块路径和模块ID
     * @param projectId         项目id
     * @param userId            userId
     */
    private void createModuleByPathIterator(Iterator<String> itemIterator, String currentModulePath, BaseTreeNode module, Map<String, String> pathMap, String projectId, String userId) {
        List<BaseTreeNode> children = module.getChildren();
        if (CollectionUtils.isEmpty(children) || !itemIterator.hasNext()) {
            //没有子节点，根据当前模块目录创建模块节点
            pathMap.put(currentModulePath, module.getId());
            if (itemIterator.hasNext()) {
                createModuleByPath(itemIterator, itemIterator.next().trim(), module, projectId, currentModulePath, pathMap, userId);
            }
            return;
        }
        String nodeName = itemIterator.next().trim();
        AtomicReference<Boolean> hasNode = new AtomicReference<>(false);
        children.forEach(child -> {
            if (StringUtils.equalsIgnoreCase(nodeName, child.getName())) {
                hasNode.set(true);
                createModuleByPathIterator(itemIterator, currentModulePath + "/" + child.getName(), child, pathMap, projectId, userId);
            }
        });

        //若子节点中不包含该目标节点，则在该节点下创建
        if (!hasNode.get()) {
            createModuleByPath(itemIterator, nodeName, module, projectId, currentModulePath, pathMap, userId);
        }

    }

    /**
     * 遍历模块路径，创建模块
     *
     * @param itemIterator 模块路径迭代器
     * @param moduleName   当前模块名称： 模块1
     * @param parentModule 父模块对象
     * @param projectId    项目id
     * @param currentPath  当前模块路径： /模块1
     * @param pathMap      记录新创建的模块路径和模块ID
     */
    private void createModuleByPath(Iterator<String> itemIterator, String moduleName, BaseTreeNode parentModule, String projectId, String currentPath, Map<String, String> pathMap, String userId) {
        StringBuilder path = new StringBuilder(currentPath);
        path.append("/" + moduleName.trim());

        //模块id
        String pid;
        if (pathMap.get(path.toString()) != null) {
            //如果创建过，直接获取模块ID
            pid = pathMap.get(path.toString());
        } else {
            pid = insertNode(moduleName, parentModule == null ? ModuleConstants.ROOT_NODE_PARENT_ID : parentModule.getId(), projectId, userId);
            pathMap.put(path.toString(), pid);
        }

        while (itemIterator.hasNext()) {
            String nextModuleName = itemIterator.next().trim();
            path.append("/" + nextModuleName);
            if (pathMap.get(path.toString()) != null) {
                pid = pathMap.get(path.toString());
            } else {
                pid = insertNode(nextModuleName, pid, projectId, userId);
                pathMap.put(path.toString(), pid);
            }
        }
    }


    /**
     * 创建模块
     *
     * @param moduleName 模块名称
     * @param parentId   父模块ID
     * @param projectId  项目ID
     * @param userId     userId
     * @return
     */
    private String insertNode(String moduleName, String parentId, String projectId, String userId) {
        FunctionalCaseModuleCreateRequest request = new FunctionalCaseModuleCreateRequest();
        request.setProjectId(projectId);
        request.setName(moduleName);
        request.setParentId(parentId);
        return this.add(request, userId);
    }


    public String getModuleName(String id) {
        if (ModuleConstants.DEFAULT_NODE_ID.equals(id)) {
            return Translator.get("functional_case.module.default.name");
        }
        return functionalCaseModuleMapper.selectByPrimaryKey(id).getName();
    }
}