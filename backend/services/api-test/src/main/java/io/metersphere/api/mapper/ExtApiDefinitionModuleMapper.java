package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.dto.debug.ApiTreeNode;
import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.interceptor.BaseConditionFilter;
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

    @BaseConditionFilter
    List<ApiTreeNode> selectApiDataByRequest(@Param("request") ApiModuleRequest request, @Param("deleted") boolean deleted);

    @BaseConditionFilter
    List<ModuleCountDTO> countModuleIdByRequest(@Param("request") ApiModuleRequest request, @Param("deleted") boolean deleted, @Param("isRepeat") boolean isRepeat);

    List<BaseTreeNode> selectNodeByIds(@Param("ids") List<String> ids);

    List<BaseTreeNode> selectBaseByIds(@Param("ids") List<String> ids);

    List<String> getModuleIdsByParentIds(@Param("parentIds") List<String> parentIds);

    List<ApiDefinitionModule> getNameInfoByIds(@Param("ids") List<String> ids);

    /**
     * 获取ApiCase的模块count
     * @param request
     * @param deleted
     * @param isRepeat
     * @return
     */
    List<ModuleCountDTO> apiCaseCountModuleIdByRequest(@Param("request") ApiModuleRequest request, @Param("deleted") boolean deleted, @Param("isRepeat") boolean isRepeat);
}
