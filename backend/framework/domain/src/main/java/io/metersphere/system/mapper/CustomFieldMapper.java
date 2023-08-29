package io.metersphere.system.mapper;

import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldMapper {
    long countByExample(CustomFieldExample example);

    int deleteByExample(CustomFieldExample example);

    int deleteByPrimaryKey(String id);

    int insert(CustomField record);

    int insertSelective(CustomField record);

    List<CustomField> selectByExample(CustomFieldExample example);

    CustomField selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CustomField record, @Param("example") CustomFieldExample example);

    int updateByExample(@Param("record") CustomField record, @Param("example") CustomFieldExample example);

    int updateByPrimaryKeySelective(CustomField record);

    int updateByPrimaryKey(CustomField record);

    int batchInsert(@Param("list") List<CustomField> list);

    int batchInsertSelective(@Param("list") List<CustomField> list, @Param("selective") CustomField.Column ... selective);
}