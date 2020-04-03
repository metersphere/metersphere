package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.testplan.QueryTestPlanRequest;
import io.metersphere.dto.ApiTestDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiTestMapper {
    List<ApiTestDTO> list(@Param("request") QueryTestPlanRequest params);
}
