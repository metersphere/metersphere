package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiTemplate;
import io.metersphere.controller.request.BaseQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtApiTemplateMapper {
    List<ApiTemplate> list(@Param("request") BaseQueryRequest request);
}
