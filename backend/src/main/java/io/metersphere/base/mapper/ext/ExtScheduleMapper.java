package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.datacount.response.TaskInfoResult;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtScheduleMapper {
    List<ScheduleDao> list(@Param("request") QueryScheduleRequest request);

    long countTaskByWorkspaceIdAndGroup(@Param("workspaceId") String workspaceId,@Param("group") String group);

    long countTaskByWorkspaceIdAndGroupAndCreateTimeRange(@Param("workspaceId")String workspaceId,@Param("group") String group, @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<TaskInfoResult> findRunningTaskInfoByWorkspaceID(String workspaceID);
}