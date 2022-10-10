package io.metersphere.base.mapper;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomFieldMapper {
    long countByExample(CustomFieldExample example);

    int deleteByExample(CustomFieldExample example);

    int deleteByPrimaryKey(String id);

    int insert(CustomField record);

    int insertSelective(CustomField record);

    List<CustomField> selectByExampleWithBLOBs(CustomFieldExample example);

    List<CustomField> selectByExample(CustomFieldExample example);

    CustomField selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CustomField record, @Param("example") CustomFieldExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomField record, @Param("example") CustomFieldExample example);

    int updateByExample(@Param("record") CustomField record, @Param("example") CustomFieldExample example);

    int updateByPrimaryKeySelective(CustomField record);

    int updateByPrimaryKeyWithBLOBs(CustomField record);

    int updateByPrimaryKey(CustomField record);
}
