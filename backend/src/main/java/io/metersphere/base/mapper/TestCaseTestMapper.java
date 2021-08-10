package io.metersphere.base.mapper;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.base.domain.TestCaseTest;
import io.metersphere.base.domain.TestCaseTestExample;
import java.util.List;

import io.metersphere.dto.LoadTestDTO;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import org.apache.ibatis.annotations.Param;

public interface TestCaseTestMapper {
    long countByExample(TestCaseTestExample example);

    int deleteByExample(TestCaseTestExample example);

    int insert(TestCaseTest record);

    int insertSelective(TestCaseTest record);

    List<TestCaseTest> selectByExample(TestCaseTestExample example);

    int updateByExampleSelective(@Param("record") TestCaseTest record, @Param("example") TestCaseTestExample example);

    int updateByExample(@Param("record") TestCaseTest record, @Param("example") TestCaseTestExample example);

    List<ApiTestCaseDTO> relevanceApiList(@Param("request") ApiTestCaseRequest request);

    List<ApiScenarioDTO> relevanceScenarioList(@Param("request") ApiScenarioRequest request);

    List<LoadTestDTO> relevanceLoadList(@Param("request") LoadCaseRequest request);
}
