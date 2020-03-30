package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.testcase.QueryTestPlanRequest;
import io.metersphere.dto.TestPlanDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanMapper {
    List<TestPlanDTO> list(@Param("request") QueryTestPlanRequest params);
}
