package io.metersphere.functional.mapper;

import io.metersphere.functional.request.FunctionalTestCaseDisassociateRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseTestMapper {

    List<String> getIds(@Param("request") FunctionalTestCaseDisassociateRequest request);


}
