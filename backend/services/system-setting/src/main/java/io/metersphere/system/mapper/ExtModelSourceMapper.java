package io.metersphere.system.mapper;

import io.metersphere.system.domain.ModelSource;
import io.metersphere.system.dto.request.ai.ModelSourceRequest;

import java.util.List;

public interface ExtModelSourceMapper {

    List<ModelSource> list(ModelSourceRequest modelSourceRequest);
}