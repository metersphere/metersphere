package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.dto.request.BugBatchUpdateRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.dto.response.BugTagEditDTO;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.project.dto.ProjectCountDTO;
import io.metersphere.project.dto.ProjectUserCreateCount;
import io.metersphere.project.dto.ProjectUserStatusCountDTO;
import io.metersphere.request.AssociateBugRequest;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ExtBugMapper {

    /**
     * 缺陷列表查询
     *
     * @param request 请求查询参数
     * @param sort    排序参数
     * @return 缺陷列表
     */
    @BaseConditionFilter
    List<BugDTO> list(@Param("request") BugPageRequest request, @Param("sort") String sort);

    /**
     * 获取项目状态流结束标识
     * @param projectId 项目ID
     * @return 结束标识集合
     */
    List<String> getLocalLastStepStatusIds(@Param("projectId") String projectId);

    /**
     * 缺陷列表查询
     *
     * @param request 请求查询参数
     * @return 缺陷列表
     */
    @BaseConditionFilter
    List<String> getIdsByPageRequest(@Param("request") BugPageRequest request);

    /**
     * 根据ID列表查询缺陷
     *
     * @param ids 缺陷ID集合
     * @param sort    排序参数
     * @return 缺陷列表
     */
    List<BugDTO> listByIds(@Param("ids") List<String> ids, @Param("sort") String sort);

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
    @BaseConditionFilter
    List<BugProviderDTO> listByProviderRequest(@Param("table") String sourceType, @Param("sourceName") String sourceName, @Param("bugColumnName") String bugColumnName, @Param("request") BugPageProviderRequest bugPageProviderRequest, @Param("deleted") boolean deleted);

    /**
     * 根据关联请求参数查询缺陷ID集合
     * @param request 关联请求参数
     * @param deleted 是否删除
     * @return 缺陷ID集合
     */
    @BaseConditionFilter
    List<String> getIdsByProvider(@Param("request") AssociateBugRequest request, @Param("deleted") boolean deleted);

    List<ProjectCountDTO> projectBugCount(@Param("projectIds") Set<String> projectIds, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userId") String userId);
    List<ProjectUserCreateCount> userCreateBugCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userIds") Set<String> userIds);

    /**
     * 根据处理人排序的处理人状态统计
     * @param projectId 项目ID
     * @param startTime 时间筛选条件
     * @param endTime 时间筛选条件
     * @param userIds 处理人筛选
     * @param platforms 平台筛选
     * @return 项目用户状态数量DTO
     */
    List<ProjectUserStatusCountDTO> projectUserBugStatusCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userIds") List<String> userIds, @Param("platforms") Set<String> platforms);

    List<Bug>getSimpleList(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("handleUsers") Set<String> handleUsers,@Param("createUser") String createUser, @Param("platforms") Set<String> platforms);

    List<Bug>getByHandleUser(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("localHandleUser") String localHandleUser,@Param("createUser") String createUser,@Param("thirdHandleUser") String thirdHandleUser, @Param("thirdPlatform") String thirdPlatform);

    long localBugCount(@Param("projectId") String projectId);

}
