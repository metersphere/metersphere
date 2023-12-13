package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionCustomField;
import io.metersphere.api.domain.ApiDefinitionCustomFieldExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDefinitionCustomFieldMapper {
    long countByExample(ApiDefinitionCustomFieldExample example);

    int deleteByExample(ApiDefinitionCustomFieldExample example);

    int deleteByPrimaryKey(@Param("apiId") String apiId, @Param("fieldId") String fieldId);

    int insert(ApiDefinitionCustomField record);

    int insertSelective(ApiDefinitionCustomField record);

    List<ApiDefinitionCustomField> selectByExample(ApiDefinitionCustomFieldExample example);

    ApiDefinitionCustomField selectByPrimaryKey(@Param("apiId") String apiId, @Param("fieldId") String fieldId);

    int updateByExampleSelective(@Param("record") ApiDefinitionCustomField record, @Param("example") ApiDefinitionCustomFieldExample example);

    int updateByExample(@Param("record") ApiDefinitionCustomField record, @Param("example") ApiDefinitionCustomFieldExample example);

    int updateByPrimaryKeySelective(ApiDefinitionCustomField record);

    int updateByPrimaryKey(ApiDefinitionCustomField record);

    int batchInsert(@Param("list") List<ApiDefinitionCustomField> list);

    int batchInsertSelective(@Param("list") List<ApiDefinitionCustomField> list, @Param("selective") ApiDefinitionCustomField.Column ... selective);
}