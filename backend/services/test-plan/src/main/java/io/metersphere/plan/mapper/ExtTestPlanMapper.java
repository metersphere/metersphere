package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.TestPlanExecuteHisDTO;
import io.metersphere.plan.dto.TestPlanGroupCountDTO;
import io.metersphere.plan.dto.TestPlanQueryConditions;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteHisPageRequest;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.project.dto.*;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ExtTestPlanMapper {
    List<String> selectIdByGroupId(String parentId);

    List<String> selectByGroupIdList(@Param("list") List<String> parentTestPlanId);

    @BaseConditionFilter
    List<TestPlanResponse> selectByConditions(@Param("request") TestPlanTableRequest request);

    List<String> selectIdByConditions(@Param("request") TestPlanBatchProcessRequest request);

    List<String> selectGroupIdByConditions(TestPlanQueryConditions testPlanQueryConditions);

    List<ModuleCountDTO> countModuleIdByConditions(@Param("request") TestPlanTableRequest testPlanQueryConditions);

    List<TestPlan> selectBaseInfoByIds(@Param("list") List<String> deleteIdList);

    long updateDefaultGroupId(@Param("list") List<String> groupIds);

    String selectProjectIdByTestPlanId(String testPlanId);

    void batchUpdateStatus(@Param("status") String status, @Param("userId") String userId, @Param("updateTime") Long updateTime, @Param("ids") List<String> ids);

    long batchMove(@Param("ids") List<String> ids, @Param("moduleId") String moduleId, @Param("userId") String userId, @Param("updateTime") long updateTime);

    List<TestPlan> getTagsByIds(@Param("ids") List<String> ids);

    void batchUpdate(@Param("testPlan") TestPlan testPlan, @Param("ids") List<String> ids);

    void setActualStartTime(@Param("id") String id, @Param("time") Long actualStartTime);

    void setActualEndTime(@Param("id") String id, @Param("time") Long actualEndTime);

    void clearActualEndTime(String id);

    List<String> selectIdByProjectId(String projectId);

    List<String> selectNotArchivedIds(@Param("ids") List<String> selectIds);

    DropNode selectDragInfoById(String s);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    long selectMaxPosByGroupId(String groupId);

    long selectMaxPosByProjectIdAndGroupId(@Param("projectId") String projectId, @Param("groupId") String groupId);

    List<TestPlanResponse> selectByGroupIds(@Param("groupIds") List<String> groupIds);

    List<String> selectRightfulIdsForExecute(@Param("ids") List<String> ids);

    List<TestPlanExecuteHisDTO> listHis(@Param("request")TestPlanExecuteHisPageRequest request);

    List<String> selectGroupIdByKeyword(@Param("projectId") String projectId, @Param("keyword") String keyword);

    List<TestPlanGroupCountDTO> countByGroupPlan(String projectId);

    List<String> selectIdByProjectIdAndWithoutList(@Param("projectId") String projectId, @Param("withoutList") List<String> withoutList);

    List<ProjectCountDTO> projectPlanCount(@Param("projectIds") Set<String> projectIds, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userId") String userId);

    List<ProjectUserCreateCount> userCreatePlanCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userIds") Set<String> userIds);


    @BaseConditionFilter
    List<TestPlanResponse> selectMyFollowByConditions(@Param("request") TestPlanTableRequest request);

    List<TestPlan> getSimpleTestPlanList(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    /**
     * 获取项目下的计划关联缺陷
     * @param projectId 项目
     * @param type 计划类型
     * @param platform 缺陷平台集合
     * @param statusList 缺陷状态
     * @return List<SelectOption>
     */
    List<SelectOption> getPlanBugList(@Param("projectId") String projectId, @Param("type") String type, @Param("platforms") List<String> platform, @Param("statusList") List<String> statusList);

    List<TestPlan> selectIdAndStatusByProjectIdAndCreateTimeRangeAndType(@Param("projectId") String projectId, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("type") String testPlanTypePlan);
}
