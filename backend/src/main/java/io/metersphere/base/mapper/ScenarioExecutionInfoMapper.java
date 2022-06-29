package io.metersphere.base.mapper;

import io.metersphere.base.domain.ScenarioExecutionInfo;
import io.metersphere.base.domain.ScenarioExecutionInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ScenarioExecutionInfoMapper {
    long countByExample(ScenarioExecutionInfoExample example);

    int deleteByExample(ScenarioExecutionInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(ScenarioExecutionInfo record);

    int insertSelective(ScenarioExecutionInfo record);

    List<ScenarioExecutionInfo> selectByExample(ScenarioExecutionInfoExample example);

    ScenarioExecutionInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ScenarioExecutionInfo record, @Param("example") ScenarioExecutionInfoExample example);

    int updateByExample(@Param("record") ScenarioExecutionInfo record, @Param("example") ScenarioExecutionInfoExample example);

    int updateByPrimaryKeySelective(ScenarioExecutionInfo record);

    int updateByPrimaryKey(ScenarioExecutionInfo record);
}