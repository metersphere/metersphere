package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.ReportRequest;
import io.metersphere.dto.ReportDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtLoadTestReportMapper {

    List<ReportDTO> getReportList(@Param("reportRequest")ReportRequest request);
}
