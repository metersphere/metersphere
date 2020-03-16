package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.testplan.QueryTestPlanRequest;
import io.metersphere.dto.FunctionalTestDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalTestMapper {
    List<FunctionalTestDTO> list(@Param("request") QueryTestPlanRequest params);
}
