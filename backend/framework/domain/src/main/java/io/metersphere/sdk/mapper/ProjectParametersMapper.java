package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.ProjectParameters;
import io.metersphere.sdk.domain.ProjectParametersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProjectParametersMapper {
    long countByExample(ProjectParametersExample example);

    int deleteByExample(ProjectParametersExample example);

    int deleteByPrimaryKey(String id);

    int insert(ProjectParameters record);

    int insertSelective(ProjectParameters record);

    List<ProjectParameters> selectByExampleWithBLOBs(ProjectParametersExample example);

    List<ProjectParameters> selectByExample(ProjectParametersExample example);

    ProjectParameters selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ProjectParameters record, @Param("example") ProjectParametersExample example);

    int updateByExampleWithBLOBs(@Param("record") ProjectParameters record, @Param("example") ProjectParametersExample example);

    int updateByExample(@Param("record") ProjectParameters record, @Param("example") ProjectParametersExample example);

    int updateByPrimaryKeySelective(ProjectParameters record);

    int updateByPrimaryKeyWithBLOBs(ProjectParameters record);

    int updateByPrimaryKey(ProjectParameters record);

    int batchInsert(@Param("list") List<ProjectParameters> list);

    int batchInsertSelective(@Param("list") List<ProjectParameters> list, @Param("selective") ProjectParameters.Column ... selective);
}