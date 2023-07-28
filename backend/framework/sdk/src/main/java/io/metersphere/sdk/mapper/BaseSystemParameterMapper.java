package io.metersphere.sdk.mapper;

import org.apache.ibatis.annotations.Param;

public interface BaseSystemParameterMapper {

    void saveBaseUrl(@Param("baseUrl") String baseUrl);
}
