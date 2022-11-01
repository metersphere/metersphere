package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

public interface BaseInformationSchemaTableMapper {

    String checkExist(@Param("tableName") String tableName);
}