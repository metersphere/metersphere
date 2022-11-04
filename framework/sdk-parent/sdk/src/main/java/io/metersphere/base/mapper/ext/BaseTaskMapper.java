package io.metersphere.base.mapper.ext;

import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.dto.TaskStatisticsDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseTaskMapper {

    List<TaskCenterDTO> getApiTasks(@Param("request") TaskCenterRequest request);
    List<TaskCenterDTO> getScenarioTasks(@Param("request") TaskCenterRequest request);
    List<TaskCenterDTO> getPerfTasks(@Param("request") TaskCenterRequest request);
    List<TaskCenterDTO> getUiTasks(@Param("request") TaskCenterRequest request);

    TaskStatisticsDTO getRunningTasks(@Param("request") TaskCenterRequest request);

    List<TaskCenterDTO> getCases(@Param("id") String id);

    List<TaskCenterDTO> getScenario(@Param("id") String id);

    List<String> checkActuator(@Param("actuator") String actuator);

    int stopScenario(@Param("request") TaskCenterRequest request);

    int stopApi(@Param("request") TaskCenterRequest request);

}