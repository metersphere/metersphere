package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiElementModule;
import io.metersphere.ui.domain.UiElementModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiElementModuleMapper {
    long countByExample(UiElementModuleExample example);

    int deleteByExample(UiElementModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiElementModule record);

    int insertSelective(UiElementModule record);

    List<UiElementModule> selectByExample(UiElementModuleExample example);

    UiElementModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiElementModule record, @Param("example") UiElementModuleExample example);

    int updateByExample(@Param("record") UiElementModule record, @Param("example") UiElementModuleExample example);

    int updateByPrimaryKeySelective(UiElementModule record);

    int updateByPrimaryKey(UiElementModule record);
}