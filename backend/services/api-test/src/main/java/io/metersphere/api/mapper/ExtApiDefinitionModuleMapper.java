package io.metersphere.api.mapper;

import io.metersphere.api.dto.debug.ApiTreeNode;
import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.request.AssociateCaseModuleProviderRequest;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionModuleMapper {
    List<BaseTreeNode> selectBaseByRequest(@Param("request") ApiModuleRequest request);

    List<BaseTreeNode> selectIdAndParentIdByRequest(@Param("request") ApiModuleRequest request);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<String> selectChildrenIdsSortByPos(String parentId);

    void deleteByIds(@Param("ids") List<String> deleteId);

    Long getMaxPosByParentId(String parentId);

    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<ApiTreeNode> selectApiDataByRequest(@Param("request") ApiModuleRequest request, @Param("deleted") boolean deleted);

    List<ModuleCountDTO> countModuleIdByRequest(@Param("request") ApiModuleRequest request, @Param("deleted") boolean deleted);

    List<BaseTreeNode> selectNodeByIds(@Param("ids") List<String> ids);

    List<BaseTreeNode> selectBaseByIds(@Param("ids") List<String> ids);

    List<ModuleCountDTO> countModuleIdByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") AssociateCaseModuleProviderRequest request, @Param("deleted") boolean deleted);

    List<BaseTreeNode> selectIdAndParentIdByProviderRequest(@Param("request") AssociateCaseModuleProviderRequest request);
}
