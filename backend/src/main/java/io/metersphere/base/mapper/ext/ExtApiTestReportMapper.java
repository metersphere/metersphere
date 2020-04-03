package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ApiReportDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTestReportMapper {

    List<ApiReportDTO> getReportList(@Param("reportRequest") ReportRequest request);

    ApiReportDTO getReportTestAndProInfo(@Param("id") String id);
}
