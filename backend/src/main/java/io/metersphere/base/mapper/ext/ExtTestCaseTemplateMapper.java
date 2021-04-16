package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCaseTemplate;
import io.metersphere.controller.request.BaseQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtTestCaseTemplateMapper {
    List<TestCaseTemplate> list(@Param("request") BaseQueryRequest request);
}
