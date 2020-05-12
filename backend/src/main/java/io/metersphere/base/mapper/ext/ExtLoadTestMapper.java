package io.metersphere.base.mapper.ext;

import io.metersphere.track.request.testplan.QueryTestPlanRequest;
import io.metersphere.dto.LoadTestDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtLoadTestMapper {
    List<LoadTestDTO> list(@Param("request") QueryTestPlanRequest params);
}
