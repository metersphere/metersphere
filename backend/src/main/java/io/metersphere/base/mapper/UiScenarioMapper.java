package io.metersphere.base.mapper;

import io.metersphere.base.domain.UiScenario;
import io.metersphere.base.domain.UiScenarioExample;
import io.metersphere.base.domain.UiScenarioWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioMapper {
    long countByExample(UiScenarioExample example);

    int deleteByExample(UiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioWithBLOBs record);

    int insertSelective(UiScenarioWithBLOBs record);

    List<UiScenarioWithBLOBs> selectByExampleWithBLOBs(UiScenarioExample example);

    List<UiScenario> selectByExample(UiScenarioExample example);

    UiScenarioWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioWithBLOBs record, @Param("example") UiScenarioExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioWithBLOBs record, @Param("example") UiScenarioExample example);

    int updateByExample(@Param("record") UiScenario record, @Param("example") UiScenarioExample example);

    int updateByPrimaryKeySelective(UiScenarioWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioWithBLOBs record);

    int updateByPrimaryKey(UiScenario record);
}