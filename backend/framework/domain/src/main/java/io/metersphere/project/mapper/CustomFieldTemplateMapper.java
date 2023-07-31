package io.metersphere.project.mapper;

import io.metersphere.project.domain.CustomFieldTemplate;
import io.metersphere.project.domain.CustomFieldTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldTemplateMapper {
    long countByExample(CustomFieldTemplateExample example);

    int deleteByExample(CustomFieldTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(CustomFieldTemplate record);

    int insertSelective(CustomFieldTemplate record);

    List<CustomFieldTemplate> selectByExampleWithBLOBs(CustomFieldTemplateExample example);

    List<CustomFieldTemplate> selectByExample(CustomFieldTemplateExample example);

    CustomFieldTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CustomFieldTemplate record, @Param("example") CustomFieldTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomFieldTemplate record, @Param("example") CustomFieldTemplateExample example);

    int updateByExample(@Param("record") CustomFieldTemplate record, @Param("example") CustomFieldTemplateExample example);

    int updateByPrimaryKeySelective(CustomFieldTemplate record);

    int updateByPrimaryKeyWithBLOBs(CustomFieldTemplate record);

    int updateByPrimaryKey(CustomFieldTemplate record);

    int batchInsert(@Param("list") List<CustomFieldTemplate> list);

    int batchInsertSelective(@Param("list") List<CustomFieldTemplate> list, @Param("selective") CustomFieldTemplate.Column ... selective);
}