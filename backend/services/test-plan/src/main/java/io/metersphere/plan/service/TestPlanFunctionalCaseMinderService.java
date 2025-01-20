package io.metersphere.plan.service;

import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.functional.service.FunctionalCaseModuleService;
import io.metersphere.plan.dto.request.TestPlanCaseBatchAddBugRequest;
import io.metersphere.plan.dto.request.TestPlanCaseBatchAssociateBugRequest;
import io.metersphere.plan.dto.request.TestPlanCaseMinderRequest;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanFunctionalCaseMinderService {

    @Resource
    private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;
    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;

    /**
     * 脑图批量添加缺陷
     *
     * @param request
     * @param bugId
     * @param userId
     */
    public void minderBatchAssociateBug(TestPlanCaseBatchAddBugRequest request, String bugId, String userId) {
        //获取脑图选中的用例id集合
        List<String> ids = getMinderSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            testPlanFunctionalCaseService.handleAssociateBug(ids, userId, bugId, request.getTestPlanId());
        }

    }

    private List<String> getMinderSelectIds(TestPlanCaseMinderRequest request) {
        if (request.isSelectAll()) {
            //全选
            List<String> ids = extTestPlanFunctionalCaseMapper.getIds(request, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            List<String> ids = new ArrayList<>();
            //项目 或 测试集
            if (CollectionUtils.isNotEmpty(request.getMinderProjectIds()) || CollectionUtils.isNotEmpty(request.getMinderCollectionIds())) {
                ids.addAll(extTestPlanFunctionalCaseMapper.selectIdsByProjectIdsOrCollectionIds(request));
            }
            //模块
            if (CollectionUtils.isNotEmpty(request.getMinderModuleIds())) {
                //处理未规划用例
                List<String> rootIds = request.getMinderModuleIds().stream().filter(id -> StringUtils.endsWith(id, "_root")).map(id -> id.replace("_root", "")).toList();
                if (CollectionUtils.isNotEmpty(rootIds)) {
                    ids.addAll(extTestPlanFunctionalCaseMapper.selectIdsByRootIds(rootIds, request.getTestPlanId()));
                }
                //获取模块及子模块
                List<FunctionalCaseModule> modules = extTestPlanFunctionalCaseMapper.selectProjectByModuleIds(request.getMinderModuleIds());
                Map<String, List<FunctionalCaseModule>> moduleMaps = modules.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getProjectId));
                List<String> minderModuleIds = new LinkedList<>();
                moduleMaps.forEach((k, v) -> {
                    buildIdsByModule(k, v, minderModuleIds);
                });
                if (CollectionUtils.isNotEmpty(minderModuleIds)) {
                    ids.addAll(extTestPlanFunctionalCaseMapper.selectIdsByModuleIds(request, minderModuleIds));
                }
            }
            //用例
            if (CollectionUtils.isNotEmpty(request.getMinderCaseIds())) {
                ids.addAll(request.getMinderCaseIds());
            }
            //去重
            return ids.stream().distinct().toList();
        }
    }

    private void buildIdsByModule(String projectId, List<FunctionalCaseModule> modules, List<String> moduleIds) {
        List<BaseTreeNode> tree = functionalCaseModuleService.getTree(projectId);
        modules.forEach(module -> {
            moduleIds.addAll(getModuleId(tree, module.getId()));
        });
    }

    private List<String> getModuleId(List<BaseTreeNode> tree, String moduleId) {
        List<String> nodeIds = new ArrayList<>();
        for (BaseTreeNode node : tree) {
            if (node.getId().equals(moduleId)) {
                nodeIds.add(node.getId());
                getChildrenModuleId(node.getChildren(), nodeIds);
            } else {
                nodeIds.addAll(getModuleId(node.getChildren(), moduleId));
            }
        }
        return nodeIds;
    }

    private void getChildrenModuleId(List<BaseTreeNode> children, List<String> nodeIds) {
        if (CollectionUtils.isNotEmpty(children)) {
            for (BaseTreeNode child : children) {
                nodeIds.add(child.getId());
                getChildrenModuleId(child.getChildren(), nodeIds);
            }
        }
    }


    /**
     * 脑图批量关联缺陷
     *
     * @param request
     * @param userId
     */
    public void batchAssociateBugByIds(TestPlanCaseBatchAssociateBugRequest request, String userId) {
        List<String> ids = getMinderSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            testPlanFunctionalCaseService.handleAssociateBugByIds(ids, request, userId);
        }

    }
}
