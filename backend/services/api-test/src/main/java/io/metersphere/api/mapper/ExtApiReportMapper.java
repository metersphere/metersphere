package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.system.dto.sdk.BasePageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiReportMapper {
    List<ApiReport> list(@Param("request") BasePageRequest request, @Param("projectId") String projectId);

}
