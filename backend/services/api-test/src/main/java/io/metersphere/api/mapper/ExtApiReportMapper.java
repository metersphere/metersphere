package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.definition.ApiReportStepDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiReportMapper {
    List<ApiReport> list(@Param("request") ApiReportPageRequest request);

    List<String> getIds(@Param("request") ApiReportBatchRequest request);

    List<ApiReport> selectApiReportByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiReportStepDTO> selectStepsByReportId(String id);
}
