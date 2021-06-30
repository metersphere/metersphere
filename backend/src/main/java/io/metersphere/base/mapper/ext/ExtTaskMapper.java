package io.metersphere.base.mapper.ext;

import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTaskMapper {

    void deleteByResourceId(String id);
    List<TaskCenterDTO> getTasks (@Param("request") TaskCenterRequest request);

    List<TaskCenterDTO> getRunningTasks (@Param("request") TaskCenterRequest request);

}