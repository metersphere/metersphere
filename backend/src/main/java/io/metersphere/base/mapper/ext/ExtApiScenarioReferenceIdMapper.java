package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReferenceIdMapper {
    List<String> selectRefIdsFromScenarioIds(@Param("ids") List<String> scenarioIds);
}
