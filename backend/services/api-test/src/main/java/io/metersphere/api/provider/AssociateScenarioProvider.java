package io.metersphere.api.provider;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.service.scenario.ApiScenarioModuleService;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.provider.BaseAssociateScenarioProvider;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("SCENARIO")
public class AssociateScenarioProvider implements BaseAssociateScenarioProvider, BaseAssociateCaseProvider {

    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ApiScenarioModuleService apiScenarioModuleService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    private static final String DEBUG_MODULE_COUNT_ALL = "all";
    private static final String UNPLANNED_SCENARIO = "api_unplanned_scenario";


    @Override
    public List<TestCaseProviderDTO> getScenarioCaseList(String sourceType, String sourceName, String caseColumnName, TestCasePageProviderRequest testCasePageProviderRequest) {
        return extApiScenarioMapper.listByProviderRequest(sourceType, sourceName, caseColumnName, testCasePageProviderRequest, false);
    }

    @Override
    public Map<String, Long> moduleCount(String sourceType, String sourceName, String apiCaseColumnName, TestCasePageProviderRequest request, boolean deleted) {
        request.setModuleIds(null);
        //查找根据moduleIds查找模块下的接口数量 查非delete状态的
        List<ModuleCountDTO> moduleCountDTOList = extApiScenarioMapper.countModuleIdByProviderRequest(sourceType, sourceName, apiCaseColumnName, request, deleted);
        long allCount = getAllCount(moduleCountDTOList);
        Map<String, Long> moduleCountMap = getModuleCountMap(request, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    @Override
    public List<ApiScenario> getSelectScenarioCases(AssociateOtherCaseRequest request, Boolean deleted) {
        if (request.isSelectAll()) {
            List<ApiScenario> cases = extApiScenarioMapper.getTestCaseByProvider(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                cases = cases.stream().filter(t -> !request.getExcludeIds().contains(t.getId())).toList();
            }
            return cases;
        } else {
            ApiScenarioExample apiScenarioExample = new ApiScenarioExample();
            apiScenarioExample.createCriteria().andIdIn(request.getSelectIds());
            return apiScenarioMapper.selectByExample(apiScenarioExample);
        }
    }


    public long getAllCount(List<ModuleCountDTO> moduleCountDTOList) {
        long count = 0;
        for (ModuleCountDTO countDTO : moduleCountDTOList) {
            count += countDTO.getDataCount();
        }
        return count;
    }

    /**
     * 查找当前项目下模块每个节点对应的资源统计
     */
    public Map<String, Long> getModuleCountMap(TestCasePageProviderRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(request, moduleCountDTOList);
        return apiScenarioModuleService.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(TestCasePageProviderRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extApiScenarioMapper.selectIdAndParentIdByProjectId(request.getProjectId());
        return apiScenarioModuleService.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get(UNPLANNED_SCENARIO));
    }

    @Override
    public List<TestCaseProviderDTO> listUnRelatedTestCaseList(TestCasePageProviderRequest request) {
        List<TestCaseProviderDTO> apiScenarios = extApiScenarioMapper.listUnRelatedCaseWithBug(request, false, request.getSortString());
        if (CollectionUtils.isEmpty(apiScenarios)) {
            return new ArrayList<>();
        }
        return apiScenarios;
    }

    @Override
    public List<String> getRelatedIdsByParam(AssociateOtherCaseRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> relatedIds = extApiScenarioMapper.getSelectIdsByAssociateParam(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                relatedIds = relatedIds.stream().filter(id -> !request.getExcludeIds().contains(id)).toList();
            }
            return relatedIds;
        } else {
            return request.getSelectIds();
        }
    }
}
