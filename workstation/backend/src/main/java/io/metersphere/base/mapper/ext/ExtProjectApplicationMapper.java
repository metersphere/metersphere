package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ProjectApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtProjectApplicationMapper {

       List<ProjectApplication>selectByProjectIdAndType(@Param("projectId")String projectId,@Param("type")String type);



}
