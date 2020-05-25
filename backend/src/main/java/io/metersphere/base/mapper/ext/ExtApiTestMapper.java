package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.QueryAPITestRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTestMapper {
    List<APITestResult> list(@Param("request") QueryAPITestRequest request);

    Long countByProjectId(String projectId);
}
