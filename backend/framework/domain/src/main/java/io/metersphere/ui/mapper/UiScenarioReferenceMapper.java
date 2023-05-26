package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioReference;
import io.metersphere.ui.domain.UiScenarioReferenceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReferenceMapper {
    long countByExample(UiScenarioReferenceExample example);

    int deleteByExample(UiScenarioReferenceExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReference record);

    int insertSelective(UiScenarioReference record);

    List<UiScenarioReference> selectByExample(UiScenarioReferenceExample example);

    UiScenarioReference selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReference record, @Param("example") UiScenarioReferenceExample example);

    int updateByExample(@Param("record") UiScenarioReference record, @Param("example") UiScenarioReferenceExample example);

    int updateByPrimaryKeySelective(UiScenarioReference record);

    int updateByPrimaryKey(UiScenarioReference record);
}