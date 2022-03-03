package io.metersphere.base.mapper;

import io.metersphere.base.domain.UiModule;
import io.metersphere.base.domain.UiModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiModuleMapper {
    long countByExample(UiModuleExample example);

    int deleteByExample(UiModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiModule record);

    int insertSelective(UiModule record);

    List<UiModule> selectByExample(UiModuleExample example);

    UiModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiModule record, @Param("example") UiModuleExample example);

    int updateByExample(@Param("record") UiModule record, @Param("example") UiModuleExample example);

    int updateByPrimaryKeySelective(UiModule record);

    int updateByPrimaryKey(UiModule record);
}