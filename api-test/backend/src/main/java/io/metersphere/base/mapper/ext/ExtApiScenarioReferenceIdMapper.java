package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiScenarioReferenceId;

import java.util.List;

public interface ExtApiScenarioReferenceIdMapper {
    List<ApiScenarioReferenceId> selectUrlByProjectId(String projectId);

    List<ApiScenarioReferenceId> selectReferenceIdByIds(List<String> ids);
}
