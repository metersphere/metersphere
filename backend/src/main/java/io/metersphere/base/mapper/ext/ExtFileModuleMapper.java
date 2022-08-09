package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiModule;
import io.metersphere.metadata.vo.FileModuleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileModuleMapper {
    int insertBatch(@Param("records") List<ApiModule> records);

    List<FileModuleVo> getNodeTreeByProjectId(@Param("projectId") String projectId);

    void updatePos(String id, Double pos);

    String getNameById(String moduleId);
}