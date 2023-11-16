package io.metersphere.api.mapper;


import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.ApiTestCasePageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Mapper
public interface ExtApiTestCaseMapper {

    Long getPos(@Param("projectId") String projectId);

    List<ApiTestCaseDTO> listByRequest(@Param("request") ApiTestCasePageRequest request, @Param("deleted") boolean deleted);
}