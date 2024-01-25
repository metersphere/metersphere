package io.metersphere.bug.mapper;

import io.metersphere.bug.dto.request.BugBatchUpdateRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.dto.response.BugTagEditDTO;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.request.AssociateBugRequest;
import io.metersphere.request.BugPageProviderRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtBugMapper {

    /**
     * 缺陷列表查询
     *
     * @param request 请求查询参数
     * @return 缺陷列表
     */
    List<BugDTO> list(@Param("request") BugPageRequest request);

    /**
     * 缺陷列表查询
     *
     * @param request 请求查询参数
     * @return 缺陷列表
     */
    List<String> getIdsByPageRequest(@Param("request") BugPageRequest request);

    /**
     * 根据ID列表查询缺陷
     *
     * @param ids 缺陷ID集合
     * @return 缺陷列表
     */
    List<BugDTO> listByIds(@Param("ids") List<String> ids);

    /**
     * 获取缺陷业务ID
     *
     * @param projectId 项目ID
     * @return 最大的业务ID
     */
    Long getMaxNum(String projectId);

    /**
     * 获取缺陷标签列表
     *
     * @param ids 缺陷ID集合
     * @return 缺陷标签列表
     */
    List<BugTagEditDTO> getBugTagList(@Param("ids") List<String> ids);

    /**
     * 批量更新缺陷
     *
     * @param request 请求参数
     * @param ids     缺陷ID集合
     */
    void batchUpdate(@Param("request") BugBatchUpdateRequest request, @Param("ids") List<String> ids);

    /**
     * 获取前置排序位置
     * @param projectId 项目ID
     * @param basePos 目标位置
     * @return 排序位置
     */
    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    /**
     * 获取后置排序位置
     * @param projectId 项目ID
     * @param basePos 目标位置
     * @return 排序位置
     */
    Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    /**
     * 获取当前项目下的最大排序位置
     * @param projectId 项目ID
     * @return 最大排序位置
     */
    Long getMaxPos(@Param("projectId") String projectId);

    /**
     * 根据关联请求参数查询缺陷集合
     * @param sourceType 资源类型
     * @param sourceName 资源名称
     * @param bugColumnName 缺陷列名
     * @param bugPageProviderRequest 关联分页请求参数
     * @param deleted 是否删除
     * @return 缺陷集合
     */
    List<BugProviderDTO> listByProviderRequest(@Param("table") String sourceType, @Param("sourceName") String sourceName, @Param("bugColumnName") String bugColumnName, @Param("request") BugPageProviderRequest bugPageProviderRequest, @Param("deleted") boolean deleted);

    /**
     * 根据关联请求参数查询缺陷ID集合
     * @param request 关联请求参数
     * @param deleted 是否删除
     * @return 缺陷ID集合
     */
    List<String> getIdsByProvider(@Param("request") AssociateBugRequest request, @Param("deleted") boolean deleted);
}
