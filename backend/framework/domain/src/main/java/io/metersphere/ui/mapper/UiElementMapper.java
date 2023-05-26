package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiElement;
import io.metersphere.ui.domain.UiElementExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiElementMapper {
    long countByExample(UiElementExample example);

    int deleteByExample(UiElementExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiElement record);

    int insertSelective(UiElement record);

    List<UiElement> selectByExample(UiElementExample example);

    UiElement selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiElement record, @Param("example") UiElementExample example);

    int updateByExample(@Param("record") UiElement record, @Param("example") UiElementExample example);

    int updateByPrimaryKeySelective(UiElement record);

    int updateByPrimaryKey(UiElement record);
}