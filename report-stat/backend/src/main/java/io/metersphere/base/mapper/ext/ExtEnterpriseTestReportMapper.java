package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.EnterpriseTestReport;
import io.metersphere.reportstatistics.dto.request.EnterpriseTestReportRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtEnterpriseTestReportMapper {
    public List<EnterpriseTestReport> selectByRequest(@Param("request") EnterpriseTestReportRequest request);

    public List<String> selectIdByRequest(@Param("request") EnterpriseTestReportRequest request);
}
