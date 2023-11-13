package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiModule;
import io.metersphere.metadata.vo.FileModuleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseFileModuleMapper {
    int insertBatch(@Param("records") List<ApiModule> records);

    List<FileModuleVo> getNodeTreeByProjectId(@Param("projectId") String projectId);

    List<FileModuleVo> getTypeNodeTreeByProjectId(@Param("projectId") String projectId, @Param("moduleType") String moduleType);

    void updatePos(String id, Double pos);

    String getNameById(String moduleId);
}