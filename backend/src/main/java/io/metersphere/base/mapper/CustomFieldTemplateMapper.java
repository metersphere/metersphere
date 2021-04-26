package io.metersphere.base.mapper;

import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.domain.CustomFieldTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldTemplateMapper {
    long countByExample(CustomFieldTemplateExample example);

    int deleteByExample(CustomFieldTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(CustomFieldTemplate record);

    int insertSelective(CustomFieldTemplate record);

    List<CustomFieldTemplate> selectByExample(CustomFieldTemplateExample example);

    CustomFieldTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CustomFieldTemplate record, @Param("example") CustomFieldTemplateExample example);

    int updateByExample(@Param("record") CustomFieldTemplate record, @Param("example") CustomFieldTemplateExample example);

    int updateByPrimaryKeySelective(CustomFieldTemplate record);

    int updateByPrimaryKey(CustomFieldTemplate record);
}