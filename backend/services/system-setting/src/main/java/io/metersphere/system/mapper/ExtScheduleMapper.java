package io.metersphere.system.mapper;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.system.dto.taskcenter.TaskCenterScheduleDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtScheduleMapper {

    /**
     * 查询任务中心定时任务列表
     *
     * @param request 列表请求参数
     * @return 定时任务列表数据
     */
    List<TaskCenterScheduleDTO> taskCenterSchedulelist(@Param("request") TaskCenterSchedulePageRequest request, @Param("projectIds") List<String> projectIds, @Param("resourceTypes") List<String> resourceTypes);

    List<ApiTestCase> getApiTestCaseListByIds(@Param("ids") List<String> ids);

    List<ApiScenario> getApiScenarioListByIds(@Param("ids") List<String> ids);

    long countByResourceId(String resourceId);

    long countByIdAndEnable(@Param("id") String scheduleId, @Param("enable") boolean isEnable);

    long countQuartzTriggersByResourceId(String scheduleId);

    long countQuartzCronTriggersByResourceId(String scheduleId);
}
