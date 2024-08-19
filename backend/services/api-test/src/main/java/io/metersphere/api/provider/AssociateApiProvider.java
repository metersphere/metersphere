package io.metersphere.api.provider;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseExample;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.definition.ApiDefinitionModuleService;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service("API")
public class AssociateApiProvider implements BaseAssociateApiProvider, BaseAssociateCaseProvider {
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Resource
    private ApiDefinitionModuleService moduleTreeService;

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;

    private static final String DEBUG_MODULE_COUNT_ALL = "all";
    private static final String UNPLANNED_API = "api_unplanned_request";

    @Override
    public List<TestCaseProviderDTO> getApiTestCaseList(String sourceType, String sourceName, String apiCaseColumnName, TestCasePageProviderRequest testCasePageProviderRequest) {
        if (CollectionUtils.isEmpty(testCasePageProviderRequest.getProtocols())) {
            return new ArrayList<>();
        }
        return extApiTestCaseMapper.listByProviderRequest(sourceType, sourceName, apiCaseColumnName, testCasePageProviderRequest, false);
    }

    @Override
    public Map<String, Long> moduleCount(String sourceType, String sourceName, String apiCaseColumnName, TestCasePageProviderRequest request, boolean deleted) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return Collections.emptyMap();
        }
        request.setModuleIds(null);
        //查找根据moduleIds查找模块下的接口数量 查非delete状态的
        List<ModuleCountDTO> moduleCountDTOList = extApiTestCaseMapper.countModuleIdByProviderRequest(sourceType, sourceName, apiCaseColumnName, request, deleted);
        long allCount = getAllCount(moduleCountDTOList);
        Map<String, Long> moduleCountMap = getModuleCountMap(request, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    @Override
    public List<ApiTestCase> getSelectApiTestCases(AssociateOtherCaseRequest request, Boolean deleted) {
        if (request.isSelectAll()) {
            List<ApiTestCase> cases = extApiTestCaseMapper.getTestCaseByProvider(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                cases = cases.stream().filter(t -> !request.getExcludeIds().contains(t.getId())).toList();
            }
            return cases;
        } else {
            ApiTestCaseExample apiTestCaseExample = new ApiTestCaseExample();
            apiTestCaseExample.createCriteria().andIdIn(request.getSelectIds());
            return apiTestCaseMapper.selectByExample(apiTestCaseExample);
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
        return moduleTreeService.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(TestCasePageProviderRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extApiTestCaseMapper.selectIdAndParentIdByProjectId(request.getProjectId());
        return moduleTreeService.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get(UNPLANNED_API));
    }


    @Override
    public List<TestCaseProviderDTO> listUnRelatedTestCaseList(TestCasePageProviderRequest request) {
        List<TestCaseProviderDTO> apiCases = extApiTestCaseMapper.listUnRelatedCaseWithBug(request, false, request.getSortString());
        if (CollectionUtils.isEmpty(apiCases)) {
            return new ArrayList<>();
        }
        return apiCases;
    }

    @Override
    public List<String> getRelatedIdsByParam(AssociateOtherCaseRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> relatedIds = extApiTestCaseMapper.getSelectIdsByAssociateParam(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                relatedIds = relatedIds.stream().filter(id -> !request.getExcludeIds().contains(id)).toList();
            }
            return relatedIds;
        } else {
            return request.getSelectIds();
        }
    }
}
