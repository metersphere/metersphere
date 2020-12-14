package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.definition.ApiComputeResult;
import io.metersphere.api.dto.definition.ApiDefinitionRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMapper {

    List<ApiDefinitionResult> list(@Param("request") ApiDefinitionRequest request);

    List<ApiComputeResult> selectByIds(@Param("ids") List<String> ids);

    int removeToGc(@Param("ids") List<String> ids);

    int reduction(@Param("ids") List<String> ids);

}