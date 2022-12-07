package io.metersphere.base.mapper.ext;


import io.metersphere.request.track.QueryTestPlanRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtLoadTestMapper {

    int moduleCount(@Param("request") QueryTestPlanRequest request);

    int getCountFollow(@Param("projectIds") List<String> projectIds,@Param("userId") String userId);

    int getCountUpcoming(@Param("projectIds") List<String> projectIds, @Param("userId") String userId);
}
