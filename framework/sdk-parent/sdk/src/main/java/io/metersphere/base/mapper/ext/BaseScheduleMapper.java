package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Schedule;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.QueryScheduleRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseScheduleMapper {
    List<ScheduleDao> list(@Param("request") QueryScheduleRequest request);

    List<Schedule> selectScenarioTaskByProjectId(String workspaceId);

    long countTaskByProjectIdAndCreateTimeRange(@Param("projectId") String projectId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<TaskInfoResult> findRunningTaskInfoByProjectID(@Param("projectId") String workspaceID, @Param("request") BaseQueryRequest request);

    int updateNameByResourceID(@Param("resourceId") String resourceId, @Param("name") String name);

}
