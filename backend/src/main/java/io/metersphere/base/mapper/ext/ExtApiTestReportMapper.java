package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.dto.ApiReportDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTestReportMapper {

    List<APIReportResult> list(@Param("request") QueryAPIReportRequest request);

    List<APIReportResult> listByTestId(@Param("testId") String testId);

    APIReportResult get(@Param("id") String id);

}
