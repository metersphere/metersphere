package io.metersphere.base.mapper;

import io.metersphere.base.domain.EnvironmentGroupProject;
import io.metersphere.base.domain.EnvironmentGroupProjectExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnvironmentGroupProjectMapper {
    long countByExample(EnvironmentGroupProjectExample example);

    int deleteByExample(EnvironmentGroupProjectExample example);

    int deleteByPrimaryKey(String id);

    int insert(EnvironmentGroupProject record);

    int insertSelective(EnvironmentGroupProject record);

    List<EnvironmentGroupProject> selectByExample(EnvironmentGroupProjectExample example);

    EnvironmentGroupProject selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") EnvironmentGroupProject record, @Param("example") EnvironmentGroupProjectExample example);

    int updateByExample(@Param("record") EnvironmentGroupProject record, @Param("example") EnvironmentGroupProjectExample example);

    int updateByPrimaryKeySelective(EnvironmentGroupProject record);

    int updateByPrimaryKey(EnvironmentGroupProject record);
}