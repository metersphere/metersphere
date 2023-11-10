package io.metersphere.api.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Mapper
public interface ExtApiTestCaseMapper {

    Long getPos(@Param("projectId") String projectId);
}