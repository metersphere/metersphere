package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCase;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.TestCaseBatchRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseMapper {

    List<TestCase> getTestCaseNames(@Param("request") QueryTestCaseRequest request);

    List<TestCaseDTO> list(@Param("request") QueryTestCaseRequest request);

    List<TestCaseDTO> listByMethod(@Param("request") QueryTestCaseRequest request);

    List<TestCaseDTO> listByTestCaseIds(@Param("request") TestCaseBatchRequest request);

    TestCase getMaxNumByProjectId(@Param("projectId") String projectId);

}
