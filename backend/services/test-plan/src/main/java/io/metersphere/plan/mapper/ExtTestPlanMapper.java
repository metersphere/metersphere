package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.TestPlanExecuteHisDTO;
import io.metersphere.plan.dto.TestPlanQueryConditions;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteHisPageRequest;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanMapper {
    List<String> selectIdByGroupId(String parentId);

    List<String> selectByGroupIdList(@Param("list") List<String> parentTestPlanId);

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

    List<TestPlanResponse> selectByGroupIds(@Param("groupIds") List<String> groupIds);

    List<String> selectRightfulIdsForExecute(@Param("ids") List<String> ids);

    List<TestPlanExecuteHisDTO> listHis(@Param("request")TestPlanExecuteHisPageRequest request);
}
