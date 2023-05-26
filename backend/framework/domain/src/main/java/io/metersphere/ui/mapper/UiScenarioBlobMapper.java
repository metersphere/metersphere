package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioBlob;
import io.metersphere.ui.domain.UiScenarioBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioBlobMapper {
    long countByExample(UiScenarioBlobExample example);

    int deleteByExample(UiScenarioBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioBlob record);

    int insertSelective(UiScenarioBlob record);

    List<UiScenarioBlob> selectByExampleWithBLOBs(UiScenarioBlobExample example);

    List<UiScenarioBlob> selectByExample(UiScenarioBlobExample example);

    UiScenarioBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioBlob record, @Param("example") UiScenarioBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioBlob record, @Param("example") UiScenarioBlobExample example);

    int updateByExample(@Param("record") UiScenarioBlob record, @Param("example") UiScenarioBlobExample example);

    int updateByPrimaryKeySelective(UiScenarioBlob record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioBlob record);
}