package io.metersphere.api.mapper;

import io.metersphere.api.dto.debug.ApiDebugRequest;
import io.metersphere.api.dto.debug.ApiTreeNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDebugModuleMapper {
    List<BaseTreeNode> selectBaseByProtocolAndUser(String userId);

    List<BaseTreeNode> selectIdAndParentIdByProtocolAndUserId(String userId);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<BaseTreeNode> selectBaseNodeByIds(@Param("ids") List<String> ids);

    List<String> selectChildrenIdsSortByPos(String parentId);

    void deleteByIds(@Param("ids") List<String> deleteId);

    Long getMaxPosByParentId(String parentId);

    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<ApiTreeNode> selectApiDebugByProtocolAndUser(String userId);

    List<ModuleCountDTO> countModuleIdByKeywordAndProtocol(@Param("request") ApiDebugRequest request, @Param("userId") String userId);
}
