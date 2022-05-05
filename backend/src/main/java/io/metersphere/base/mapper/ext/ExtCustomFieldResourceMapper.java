package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ext.CustomFieldResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCustomFieldResourceMapper {

    int insert(@Param("tableName") String tableName, @Param("record") CustomFieldResource record);

    int updateByPrimaryKeySelective(@Param("tableName") String tableName, @Param("record") CustomFieldResource record);

    void deleteByResourceId(@Param("tableName") String tableName, @Param("resourceId") String resourceId);

    void deleteByResourceIds(@Param("tableName") String tableName, @Param("resourceIds") List<String> resourceIds);

    List<CustomFieldResource> getByResourceId(@Param("tableName") String tableName, @Param("resourceId") String resourceId);

    List<CustomFieldResource> getByResourceIds(@Param("tableName")  String tableName, @Param("resourceIds") List<String> resourceIds);

    long countFieldResource(@Param("tableName") String tableName, @Param("resourceId") String resourceId, @Param("fieldId") String field_id);
}
