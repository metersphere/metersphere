package io.metersphere.base.mapper;

import io.metersphere.base.domain.FieldTemplate;
import io.metersphere.base.domain.FieldTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FieldTemplateMapper {
    long countByExample(FieldTemplateExample example);

    int deleteByExample(FieldTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(FieldTemplate record);

    int insertSelective(FieldTemplate record);

    List<FieldTemplate> selectByExample(FieldTemplateExample example);

    FieldTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FieldTemplate record, @Param("example") FieldTemplateExample example);

    int updateByExample(@Param("record") FieldTemplate record, @Param("example") FieldTemplateExample example);

    int updateByPrimaryKeySelective(FieldTemplate record);

    int updateByPrimaryKey(FieldTemplate record);
}