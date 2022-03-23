package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.domain.TestCaseNodeExample;
import io.metersphere.track.dto.ModuleNodeDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtModuleNodeMapper {
    int insertSelective(@Param("tableName") String tableName, @Param("record") ModuleNode record);

    List<ModuleNode> selectByExample(@Param("tableName") String tableName, @Param("example") TestCaseNodeExample example);

    List<String> getNodeIdsByPid(@Param("tableName") String tableName, @Param("parentId") String parentId);

    List<ModuleNodeDTO> getNodeTreeByProjectId(@Param("tableName") String tableName, @Param("projectId") String projectId);

    int updateByPrimaryKeySelective(@Param("tableName") String tableName, @Param("record") ModuleNode record);

    int deleteByExample(@Param("tableName") String tableName, @Param("example") TestCaseNodeExample example);

    void insert(@Param("tableName") String tableName, @Param("record") ModuleNode record);

    ModuleNodeDTO get(@Param("tableName") String tableName, @Param("id") String id);

    void updatePos(@Param("tableName") String tableName, @Param("id") String id, @Param("pos")  Double pos);

    void updateByPrimaryKey(@Param("tableName") String tableName, @Param("record") ModuleNode record);

    ModuleNode selectByPrimaryKey(@Param("tableName") String tableName, @Param("id") String id);

    long countByExample(@Param("tableName") String tableName, @Param("example") TestCaseNodeExample example);
}
