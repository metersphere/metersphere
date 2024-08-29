package io.metersphere.plan.service;

import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.functional.service.FunctionalCaseModuleService;
import io.metersphere.plan.dto.request.TestPlanCaseMinderBatchAddBugRequest;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;

    public void minderBatchAssociateBug(TestPlanCaseMinderBatchAddBugRequest request, String bugId, String userId) {
        //获取脑图选中的用例id集合
        List<String> ids = getMinderSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 500, (subList) -> {
                Map<String, String> caseMap = testPlanFunctionalCaseService.getCaseMap(subList);
                List<BugRelationCase> list = new ArrayList<>();
                subList.forEach(id -> {
                    BugRelationCase bugRelationCase = new BugRelationCase();
                    bugRelationCase.setId(IDGenerator.nextStr());
                    bugRelationCase.setBugId(bugId);
                    bugRelationCase.setCaseId(caseMap.get(id));
                    bugRelationCase.setCaseType(CaseType.FUNCTIONAL_CASE.getKey());
                    bugRelationCase.setCreateUser(userId);
                    bugRelationCase.setCreateTime(System.currentTimeMillis());
                    bugRelationCase.setUpdateTime(System.currentTimeMillis());
                    bugRelationCase.setTestPlanCaseId(id);
                    bugRelationCase.setTestPlanId(request.getTestPlanId());
                    list.add(bugRelationCase);
                });
                bugRelationCaseMapper.batchInsert(list);
            });
        }

    }

    private List<String> getMinderSelectIds(TestPlanCaseMinderBatchAddBugRequest request) {
        if (request.isSelectAll()) {
            //全选
            List<String> ids = extTestPlanFunctionalCaseMapper.getIds(request, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            List<String> ids = new ArrayList<>();
            //项目
            if (CollectionUtils.isNotEmpty(request.getMinderProjectIds())) {
                ids.addAll(extTestPlanFunctionalCaseMapper.selectIdsByProjectIds(request));
            }
            //模块
            if (CollectionUtils.isNotEmpty(request.getMinderModuleIds())) {
                //获取模块及子模块
                List<FunctionalCaseModule> modules = extTestPlanFunctionalCaseMapper.selectProjectByModuleIds(request.getMinderModuleIds());
                Map<String, List<FunctionalCaseModule>> moduleMaps = modules.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getProjectId));
                List<String> minderModuleIds = new LinkedList<>();
                moduleMaps.forEach((k, v) -> {
                    buildIdsByModule(k, v, minderModuleIds);
                });

                ids.addAll(extTestPlanFunctionalCaseMapper.selectIdsByModuleIds(request, minderModuleIds));
            }
            //用例
            if (CollectionUtils.isNotEmpty(request.getMinderCaseIds())) {
                ids.addAll(request.getMinderCaseIds());
            }
            return ids;
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
                getModuleId(node.getChildren(), moduleId);
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


}
