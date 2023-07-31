package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiElementScenarioReference;
import io.metersphere.ui.domain.UiElementScenarioReferenceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiElementScenarioReferenceMapper {
    long countByExample(UiElementScenarioReferenceExample example);

    int deleteByExample(UiElementScenarioReferenceExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiElementScenarioReference record);

    int insertSelective(UiElementScenarioReference record);

    List<UiElementScenarioReference> selectByExample(UiElementScenarioReferenceExample example);

    UiElementScenarioReference selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiElementScenarioReference record, @Param("example") UiElementScenarioReferenceExample example);

    int updateByExample(@Param("record") UiElementScenarioReference record, @Param("example") UiElementScenarioReferenceExample example);

    int updateByPrimaryKeySelective(UiElementScenarioReference record);

    int updateByPrimaryKey(UiElementScenarioReference record);

    int batchInsert(@Param("list") List<UiElementScenarioReference> list);

    int batchInsertSelective(@Param("list") List<UiElementScenarioReference> list, @Param("selective") UiElementScenarioReference.Column ... selective);
}