package io.metersphere.api.provider;

import io.metersphere.api.mapper.ExtApiDefinitionModuleMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.definition.ApiDefinitionModuleService;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.request.ApiModuleProviderRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class AssociateApiProvider implements BaseAssociateApiProvider {
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Resource
    private ExtApiDefinitionModuleMapper extApiDefinitionModuleMapper;

    @Resource
    private ApiDefinitionModuleService moduleTreeService;

    private static final String DEBUG_MODULE_COUNT_ALL = "all";
    private static final String UNPLANNED_API = "api_unplanned_request";

    @Override
    public List<TestCaseProviderDTO> getApiTestCaseList(String sourceType, String sourceName, String apiCaseColumnName, TestCasePageProviderRequest testCasePageProviderRequest) {
        return extApiTestCaseMapper.listByProviderRequest(sourceType, sourceName, apiCaseColumnName, testCasePageProviderRequest, false);
    }

    @Override
    public Map<String, Long> moduleCount(String sourceType, String sourceName, String apiCaseColumnName, ApiModuleProviderRequest request, boolean deleted) {
        request.setModuleIds(null);
        //查找根据moduleIds查找模块下的接口数量 查非delete状态的
        List<ModuleCountDTO> moduleCountDTOList = extApiDefinitionModuleMapper.countModuleIdByProviderRequest(sourceType, sourceName, apiCaseColumnName, request, deleted);
        long allCount = getAllCount(moduleCountDTOList);
        Map<String, Long> moduleCountMap = getModuleCountMap(request, moduleCountDTOList);
        moduleCountMap.put(DEBUG_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    @Override
    public List<String> getSelectIds(AssociateOtherCaseRequest request, Boolean deleted) {
        if (request.isSelectAll()) {
            List<String> ids = extApiTestCaseMapper.getIdsByProvider(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
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
    public Map<String, Long> getModuleCountMap(ApiModuleProviderRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = this.getTreeOnlyIdsAndResourceCount(request, moduleCountDTOList);
        return moduleTreeService.getIdCountMapByBreadth(treeNodeList);
    }

    public List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(ApiModuleProviderRequest request, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<BaseTreeNode> fileModuleList = extApiDefinitionModuleMapper.selectIdAndParentIdByProviderRequest(request);
        return moduleTreeService.buildTreeAndCountResource(fileModuleList, moduleCountDTOList, true, Translator.get(UNPLANNED_API));
    }

}
