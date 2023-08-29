package io.metersphere.system.mapper;

import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.domain.CustomFieldOptionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldOptionMapper {
    long countByExample(CustomFieldOptionExample example);

    int deleteByExample(CustomFieldOptionExample example);

    int deleteByPrimaryKey(@Param("fieldId") String fieldId, @Param("value") String value);

    int insert(CustomFieldOption record);

    int insertSelective(CustomFieldOption record);

    List<CustomFieldOption> selectByExample(CustomFieldOptionExample example);

    CustomFieldOption selectByPrimaryKey(@Param("fieldId") String fieldId, @Param("value") String value);

    int updateByExampleSelective(@Param("record") CustomFieldOption record, @Param("example") CustomFieldOptionExample example);

    int updateByExample(@Param("record") CustomFieldOption record, @Param("example") CustomFieldOptionExample example);

    int updateByPrimaryKeySelective(CustomFieldOption record);

    int updateByPrimaryKey(CustomFieldOption record);

    int batchInsert(@Param("list") List<CustomFieldOption> list);

    int batchInsertSelective(@Param("list") List<CustomFieldOption> list, @Param("selective") CustomFieldOption.Column ... selective);
}