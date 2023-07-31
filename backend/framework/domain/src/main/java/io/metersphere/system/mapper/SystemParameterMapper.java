package io.metersphere.system.mapper;

import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.domain.SystemParameterExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SystemParameterMapper {
    long countByExample(SystemParameterExample example);

    int deleteByExample(SystemParameterExample example);

    int deleteByPrimaryKey(String paramKey);

    int insert(SystemParameter record);

    int insertSelective(SystemParameter record);

    List<SystemParameter> selectByExample(SystemParameterExample example);

    SystemParameter selectByPrimaryKey(String paramKey);

    int updateByExampleSelective(@Param("record") SystemParameter record, @Param("example") SystemParameterExample example);

    int updateByExample(@Param("record") SystemParameter record, @Param("example") SystemParameterExample example);

    int updateByPrimaryKeySelective(SystemParameter record);

    int updateByPrimaryKey(SystemParameter record);

    int batchInsert(@Param("list") List<SystemParameter> list);

    int batchInsertSelective(@Param("list") List<SystemParameter> list, @Param("selective") SystemParameter.Column ... selective);
}