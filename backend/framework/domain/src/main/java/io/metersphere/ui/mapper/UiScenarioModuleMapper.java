package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioModule;
import io.metersphere.ui.domain.UiScenarioModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioModuleMapper {
    long countByExample(UiScenarioModuleExample example);

    int deleteByExample(UiScenarioModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioModule record);

    int insertSelective(UiScenarioModule record);

    List<UiScenarioModule> selectByExample(UiScenarioModuleExample example);

    UiScenarioModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioModule record, @Param("example") UiScenarioModuleExample example);

    int updateByExample(@Param("record") UiScenarioModule record, @Param("example") UiScenarioModuleExample example);

    int updateByPrimaryKeySelective(UiScenarioModule record);

    int updateByPrimaryKey(UiScenarioModule record);
}