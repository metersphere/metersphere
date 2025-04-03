package io.metersphere.system.mapper;

import io.metersphere.system.domain.ModelSource;
import io.metersphere.system.dto.request.ai.ModelSourceRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtModelSourceMapper {

    List<ModelSource> list(@Param("request") ModelSourceRequest modelSourceRequest);
}