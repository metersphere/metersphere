package io.metersphere.base.mapper;

import io.metersphere.base.domain.WorkspaceRepository;
import io.metersphere.base.domain.WorkspaceRepositoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WorkspaceRepositoryMapper {
    long countByExample(WorkspaceRepositoryExample example);

    int deleteByExample(WorkspaceRepositoryExample example);

    int deleteByPrimaryKey(String id);

    int insert(WorkspaceRepository record);

    int insertSelective(WorkspaceRepository record);

    List<WorkspaceRepository> selectByExampleWithBLOBs(WorkspaceRepositoryExample example);

    List<WorkspaceRepository> selectByExample(WorkspaceRepositoryExample example);

    WorkspaceRepository selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") WorkspaceRepository record, @Param("example") WorkspaceRepositoryExample example);

    int updateByExampleWithBLOBs(@Param("record") WorkspaceRepository record, @Param("example") WorkspaceRepositoryExample example);

    int updateByExample(@Param("record") WorkspaceRepository record, @Param("example") WorkspaceRepositoryExample example);

    int updateByPrimaryKeySelective(WorkspaceRepository record);

    int updateByPrimaryKeyWithBLOBs(WorkspaceRepository record);

    int updateByPrimaryKey(WorkspaceRepository record);
}