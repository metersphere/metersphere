package io.metersphere.sdk.mapper;


import org.apache.ibatis.annotations.Param;

public interface BaseModuleMapper {
    long addResourceCount(@Param("tableName") String tableName, @Param("primaryKey") String primaryKey, @Param("count") int count);
    long subResourceCount(@Param("tableName") String tableName, @Param("primaryKey") String primaryKey, @Param("count") int count);
}