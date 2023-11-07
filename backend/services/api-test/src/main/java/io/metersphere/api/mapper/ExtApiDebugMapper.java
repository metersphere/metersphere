package io.metersphere.api.mapper;


import io.metersphere.api.dto.debug.ApiDebugSimpleDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Mapper
public interface ExtApiDebugMapper {
    List<ApiDebugSimpleDTO> list(@Param("protocol") String protocol, @Param("userId") String userId);
}