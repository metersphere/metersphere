package io.metersphere.project.mapper;

import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.customfunction.request.CustomFunctionPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: LAN
 * @date: 2024/1/11 15:45
 * @version: 1.0
 */
public interface ExtCustomFunctionMapper {
    List<CustomFunctionDTO> list(@Param("request") CustomFunctionPageRequest request);
}
