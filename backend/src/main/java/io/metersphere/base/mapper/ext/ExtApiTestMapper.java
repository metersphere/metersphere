package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.base.domain.ApiTest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ExtApiTestMapper {
    List<APITestResult> list(@Param("request") QueryAPITestRequest request);

    List<ApiTest> getApiTestByProjectId(String projectId);

    List<ApiTest> listByIds(@Param("ids") List<String> ids);

    int checkApiTestOwner(@Param("testId") String testId, @Param("projectIds") Set<String> projectIds);

}
