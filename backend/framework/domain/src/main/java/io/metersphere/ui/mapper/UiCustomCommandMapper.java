package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiCustomCommand;
import io.metersphere.ui.domain.UiCustomCommandExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiCustomCommandMapper {
    long countByExample(UiCustomCommandExample example);

    int deleteByExample(UiCustomCommandExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiCustomCommand record);

    int insertSelective(UiCustomCommand record);

    List<UiCustomCommand> selectByExample(UiCustomCommandExample example);

    UiCustomCommand selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiCustomCommand record, @Param("example") UiCustomCommandExample example);

    int updateByExample(@Param("record") UiCustomCommand record, @Param("example") UiCustomCommandExample example);

    int updateByPrimaryKeySelective(UiCustomCommand record);

    int updateByPrimaryKey(UiCustomCommand record);
}