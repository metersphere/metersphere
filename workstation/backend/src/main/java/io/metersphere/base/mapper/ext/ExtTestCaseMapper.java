package io.metersphere.base.mapper.ext;


import io.metersphere.request.track.QueryTestCaseRequest;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseMapper {

    int moduleCount(@Param("request") QueryTestCaseRequest request);

    int getCountFollow(@Param("projectIds") List<String> projectIds,@Param("userId") String userId);

    int getCountUpcoming(@Param("projectIds") List<String> projectIds, @Param("userId") String userId);
}
