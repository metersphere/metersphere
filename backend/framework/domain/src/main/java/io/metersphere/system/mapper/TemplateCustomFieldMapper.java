package io.metersphere.system.mapper;

import io.metersphere.system.domain.TemplateCustomField;
import io.metersphere.system.domain.TemplateCustomFieldExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TemplateCustomFieldMapper {
    long countByExample(TemplateCustomFieldExample example);

    int deleteByExample(TemplateCustomFieldExample example);

    int deleteByPrimaryKey(String id);

    int insert(TemplateCustomField record);

    int insertSelective(TemplateCustomField record);

    List<TemplateCustomField> selectByExample(TemplateCustomFieldExample example);

    TemplateCustomField selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TemplateCustomField record, @Param("example") TemplateCustomFieldExample example);

    int updateByExample(@Param("record") TemplateCustomField record, @Param("example") TemplateCustomFieldExample example);

    int updateByPrimaryKeySelective(TemplateCustomField record);

    int updateByPrimaryKey(TemplateCustomField record);

    int batchInsert(@Param("list") List<TemplateCustomField> list);

    int batchInsertSelective(@Param("list") List<TemplateCustomField> list, @Param("selective") TemplateCustomField.Column ... selective);
}