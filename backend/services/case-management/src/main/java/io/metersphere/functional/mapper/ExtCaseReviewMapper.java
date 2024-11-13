package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.request.CaseReviewBatchRequest;
import io.metersphere.functional.request.CaseReviewPageRequest;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.ProjectCountDTO;
import io.metersphere.project.dto.ProjectUserCreateCount;
import io.metersphere.project.dto.ProjectUserStatusCountDTO;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author guoyuqi
 */
public interface ExtCaseReviewMapper {

    List<CaseReview> checkCaseByModuleIds(@Param("moduleIds") List<String> deleteIds);

    Long getPos(@Param("projectId") String projectId);

    @BaseConditionFilter
    List<CaseReviewDTO> list(@Param("request") CaseReviewPageRequest request);

    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    @BaseConditionFilter
    List<String> getIds(@Param("request") CaseReviewBatchRequest request, @Param("projectId") String projectId);

    void batchMoveModule(@Param("request") CaseReviewBatchRequest request, @Param("ids") List<String> ids, @Param("userId") String userId);

    @BaseConditionFilter
    List<ModuleCountDTO> countModuleIdByKeywordAndFileType(@Param("request") CaseReviewPageRequest request);

    @BaseConditionFilter
    long caseCount(@Param("request") CaseReviewPageRequest request);


    String getReviewPassRule(@Param("id") String id);

    List<ProjectCountDTO> projectReviewCount(@Param("projectIds") Set<String> projectIds, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userId") String userId);

    List<ProjectUserCreateCount> userCreateReviewCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userIds") Set<String> userIds);

    /**
     * 获取各种状态总数量的评审
     * @param projectId 项目ID
     * @param startTime 时间过滤条件
     * @param endTime 时间过滤条件
     * @return ProjectUserStatusCountDTO userId 在这里不返回
     */
    List<ProjectUserStatusCountDTO> statusReviewCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

}
