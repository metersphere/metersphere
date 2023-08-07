package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioVariable;
import io.metersphere.ui.domain.UiScenarioVariableExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioVariableMapper {
    long countByExample(UiScenarioVariableExample example);

    int deleteByExample(UiScenarioVariableExample example);

    int deleteByPrimaryKey(String resourceId);

    int insert(UiScenarioVariable record);

    int insertSelective(UiScenarioVariable record);

    List<UiScenarioVariable> selectByExample(UiScenarioVariableExample example);

    UiScenarioVariable selectByPrimaryKey(String resourceId);

    int updateByExampleSelective(@Param("record") UiScenarioVariable record, @Param("example") UiScenarioVariableExample example);

    int updateByExample(@Param("record") UiScenarioVariable record, @Param("example") UiScenarioVariableExample example);

    int updateByPrimaryKeySelective(UiScenarioVariable record);

    int updateByPrimaryKey(UiScenarioVariable record);

    int batchInsert(@Param("list") List<UiScenarioVariable> list);

    int batchInsertSelective(@Param("list") List<UiScenarioVariable> list, @Param("selective") UiScenarioVariable.Column ... selective);
}