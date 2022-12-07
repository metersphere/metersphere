package io.metersphere.base.mapper.ext;


import io.metersphere.request.api.ApiScenarioRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioMapper {

    int listModule(@Param("request") ApiScenarioRequest request);

    int getCountFollow(@Param("projectIds") List<String> projectIds,@Param("userId") String userId);

    int getCountUpcoming(@Param("projectIds") List<String> projectIds, @Param("userId") String userId);

}
