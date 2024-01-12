package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.ProjectParameter;
import io.metersphere.sdk.domain.ProjectParameterExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProjectParameterMapper {
    long countByExample(ProjectParameterExample example);

    int deleteByExample(ProjectParameterExample example);

    int deleteByPrimaryKey(String id);

    int insert(ProjectParameter record);

    int insertSelective(ProjectParameter record);

    List<ProjectParameter> selectByExampleWithBLOBs(ProjectParameterExample example);

    List<ProjectParameter> selectByExample(ProjectParameterExample example);

    ProjectParameter selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ProjectParameter record, @Param("example") ProjectParameterExample example);

    int updateByExampleWithBLOBs(@Param("record") ProjectParameter record, @Param("example") ProjectParameterExample example);

    int updateByExample(@Param("record") ProjectParameter record, @Param("example") ProjectParameterExample example);

    int updateByPrimaryKeySelective(ProjectParameter record);

    int updateByPrimaryKeyWithBLOBs(ProjectParameter record);

    int updateByPrimaryKey(ProjectParameter record);

    int batchInsert(@Param("list") List<ProjectParameter> list);

    int batchInsertSelective(@Param("list") List<ProjectParameter> list, @Param("selective") ProjectParameter.Column... selective);
}