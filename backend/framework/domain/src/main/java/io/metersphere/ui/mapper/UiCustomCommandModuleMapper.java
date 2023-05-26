package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiCustomCommandModule;
import io.metersphere.ui.domain.UiCustomCommandModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiCustomCommandModuleMapper {
    long countByExample(UiCustomCommandModuleExample example);

    int deleteByExample(UiCustomCommandModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiCustomCommandModule record);

    int insertSelective(UiCustomCommandModule record);

    List<UiCustomCommandModule> selectByExample(UiCustomCommandModuleExample example);

    UiCustomCommandModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiCustomCommandModule record, @Param("example") UiCustomCommandModuleExample example);

    int updateByExample(@Param("record") UiCustomCommandModule record, @Param("example") UiCustomCommandModuleExample example);

    int updateByPrimaryKeySelective(UiCustomCommandModule record);

    int updateByPrimaryKey(UiCustomCommandModule record);
}