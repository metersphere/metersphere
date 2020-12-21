package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiDefinitionExecResult;

public interface ExtApiDefinitionExecResultMapper {

    void deleteByResourceId(String id);

    ApiDefinitionExecResult selectMaxResultByResourceId(String resourceId);

    ApiDefinitionExecResult selectMaxResultByResourceIdAndType(String resourceId, String type);
}