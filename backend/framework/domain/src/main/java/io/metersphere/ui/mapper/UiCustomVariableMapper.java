package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiCustomVariable;
import io.metersphere.ui.domain.UiCustomVariableExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiCustomVariableMapper {
    long countByExample(UiCustomVariableExample example);

    int deleteByExample(UiCustomVariableExample example);

    int deleteByPrimaryKey(String resourceId);

    int insert(UiCustomVariable record);

    int insertSelective(UiCustomVariable record);

    List<UiCustomVariable> selectByExample(UiCustomVariableExample example);

    UiCustomVariable selectByPrimaryKey(String resourceId);

    int updateByExampleSelective(@Param("record") UiCustomVariable record, @Param("example") UiCustomVariableExample example);

    int updateByExample(@Param("record") UiCustomVariable record, @Param("example") UiCustomVariableExample example);

    int updateByPrimaryKeySelective(UiCustomVariable record);

    int updateByPrimaryKey(UiCustomVariable record);

    int batchInsert(@Param("list") List<UiCustomVariable> list);

    int batchInsertSelective(@Param("list") List<UiCustomVariable> list, @Param("selective") UiCustomVariable.Column ... selective);
}