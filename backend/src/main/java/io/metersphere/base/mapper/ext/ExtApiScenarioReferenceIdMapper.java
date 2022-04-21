package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiScenarioReferenceId;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReferenceIdMapper {
    List<ApiScenarioReferenceId> selectUrlByProjectId(String projectId);
}
