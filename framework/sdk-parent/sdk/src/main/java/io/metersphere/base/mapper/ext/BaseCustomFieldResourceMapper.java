package io.metersphere.base.mapper.ext;

import io.metersphere.dto.CustomFieldResourceDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseCustomFieldResourceMapper {

    int insert(@Param("tableName") String tableName, @Param("record") CustomFieldResourceDTO record);

    int updateByPrimaryKeySelective(@Param("tableName") String tableName, @Param("record") CustomFieldResourceDTO record);

    void deleteByResourceId(@Param("tableName") String tableName, @Param("resourceId") String resourceId);

    void deleteByResourceIds(@Param("tableName") String tableName, @Param("resourceIds") List<String> resourceIds);

    List<CustomFieldResourceDTO> getByResourceId(@Param("tableName") String tableName, @Param("resourceId") String resourceId);

    List<CustomFieldResourceDTO> getByResourceIds(@Param("tableName")  String tableName, @Param("resourceIds") List<String> resourceIds);

    List<CustomFieldResourceDTO> getByResourceIdsForList(@Param("tableName")  String tableName, @Param("resourceIds") List<String> resourceIds);

    long countFieldResource(@Param("tableName") String tableName, @Param("resourceId") String resourceId, @Param("fieldId") String field_id);

    int batchUpdateByResourceIds(@Param("tableName") String tableName, @Param("resourceIds") List<String> resourceIds, @Param("record") CustomFieldResourceDTO customField);

    void batchInsertIfNotExists(@Param("tableName") String tableName, @Param("record") CustomFieldResourceDTO customField);
}
