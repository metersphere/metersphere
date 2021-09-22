package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.CustomFunction;
import io.metersphere.controller.request.CustomFunctionRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCustomFunctionMapper {

    List<CustomFunction> queryAll(@Param("request")CustomFunctionRequest request);
}
