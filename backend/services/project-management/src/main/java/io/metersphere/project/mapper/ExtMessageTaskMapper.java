package io.metersphere.project.mapper;

import io.metersphere.project.domain.MessageTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtMessageTaskMapper {

    List<MessageTask> getRobotType(@Param("projectId") String projectId, @Param("taskType") String taskType, @Param("event") String event);
}
