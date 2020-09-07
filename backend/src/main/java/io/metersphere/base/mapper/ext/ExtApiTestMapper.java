package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.base.domain.ApiTest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTestMapper {
    List<APITestResult> list(@Param("request") QueryAPITestRequest request);

    List<ApiTest> getApiTestByProjectId(String projectId);

    List<ApiTest> listByIds(@Param("ids") List<String> ids);
}
