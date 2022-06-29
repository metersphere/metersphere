package io.metersphere.base.mapper;

import io.metersphere.base.domain.UiElementReference;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UiElementReferenceMapper {


    void batchInsert(@Param("uiElementReferences") List<UiElementReference> uiElementReferences);

    List<UiElementReference> listByElementIds(@Param("projectId") String projectId, @Param("scenarioId") String scenarioId, @Param("elementIds") List<String> elementIds, @Param("limit") Integer limit);

    void batchDelete(@Param("projectId") String projectId, @Param("scenarioId") String scenarioId, @Param("elementIds") List<String> elementIds);

    List<UiElementReference> listByScenarioId(@Param("projectId") String projectId, @Param("scenarioId") String scenarioId);

    List<UiElementReference> listByModuleIds(String projectId, List<String> moduleIds, Integer limit);

    void batchDeleteWithScenarioIds(@Param("scenarioIds") List<String> scenarioIds);
}
