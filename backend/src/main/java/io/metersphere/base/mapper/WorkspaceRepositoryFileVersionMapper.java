package io.metersphere.base.mapper;

import io.metersphere.base.domain.WorkspaceRepositoryFileVersion;
import io.metersphere.base.domain.WorkspaceRepositoryFileVersionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WorkspaceRepositoryFileVersionMapper {
    long countByExample(WorkspaceRepositoryFileVersionExample example);

    int deleteByExample(WorkspaceRepositoryFileVersionExample example);

    int deleteByPrimaryKey(String id);

    int insert(WorkspaceRepositoryFileVersion record);

    int insertSelective(WorkspaceRepositoryFileVersion record);

    List<WorkspaceRepositoryFileVersion> selectByExample(WorkspaceRepositoryFileVersionExample example);

    WorkspaceRepositoryFileVersion selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") WorkspaceRepositoryFileVersion record, @Param("example") WorkspaceRepositoryFileVersionExample example);

    int updateByExample(@Param("record") WorkspaceRepositoryFileVersion record, @Param("example") WorkspaceRepositoryFileVersionExample example);

    int updateByPrimaryKeySelective(WorkspaceRepositoryFileVersion record);

    int updateByPrimaryKey(WorkspaceRepositoryFileVersion record);
}