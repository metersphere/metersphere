package io.metersphere.base.mapper.ext;

import io.metersphere.request.api.ApiTestCaseRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTestCaseMapper {

    int moduleCount(@Param("request") ApiTestCaseRequest request);

    int getCountFollow(@Param("projectIds") List<String> projectIds, @Param("userId") String userId);

    int getUpdateCount(@Param("userId") String userId,@Param("projectId") String projectId,@Param("statusList") List<String> statusList,@Param("toBeUpdateTime") Long toBeUpdatedTime);


    int getCountUpcoming(@Param("projectIds") List<String> projectIds, @Param("userId") String userId);
}
