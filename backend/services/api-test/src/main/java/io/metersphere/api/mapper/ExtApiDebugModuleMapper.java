package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDebugModule;
import io.metersphere.api.request.debug.ApiDebugRequest;
import io.metersphere.api.request.debug.ApiTreeNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDebugModuleMapper {
    List<BaseTreeNode> selectBaseByProtocolAndUser(String protocol, String userId);

    List<BaseTreeNode> selectIdAndParentIdByProtocolAndUserId(String protocol, String userId);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<String> selectChildrenIdsSortByPos(String parentId);

    void deleteByIds(@Param("ids") List<String> deleteId);

    Integer getMaxPosByParentId(String parentId);

    List<String> selectIdsByProjectId(String projectId);

    ApiDebugModule getLastModuleByParentId(String id);

    ApiDebugModule getNextModuleInParentId(@Param("parentId") String parentId, @Param("pos") int pos);

    ApiDebugModule getPreviousModuleInParentId(@Param("parentId") String parentId, @Param("pos") int pos);

    List<ApiTreeNode> selectApiDebugByProtocolAndUser(String protocol, String userId);

    List<ModuleCountDTO> countModuleIdByKeywordAndProtocol(@Param("request") ApiDebugRequest request, @Param("userId") String userId);
}
