package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCaseFieldTemplate;
import io.metersphere.controller.request.BaseQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseFieldTemplateMapper {
    List<TestCaseFieldTemplate> list(@Param("request") BaseQueryRequest request);
}
