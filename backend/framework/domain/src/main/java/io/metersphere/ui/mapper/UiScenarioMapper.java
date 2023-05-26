package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenario;
import io.metersphere.ui.domain.UiScenarioExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioMapper {
    long countByExample(UiScenarioExample example);

    int deleteByExample(UiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenario record);

    int insertSelective(UiScenario record);

    List<UiScenario> selectByExample(UiScenarioExample example);

    UiScenario selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenario record, @Param("example") UiScenarioExample example);

    int updateByExample(@Param("record") UiScenario record, @Param("example") UiScenarioExample example);

    int updateByPrimaryKeySelective(UiScenario record);

    int updateByPrimaryKey(UiScenario record);
}