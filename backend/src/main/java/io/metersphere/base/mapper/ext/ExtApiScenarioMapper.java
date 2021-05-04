package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiScenarioExample;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioRequest request);
    List<ApiScenarioDTO> listReview(@Param("request") ApiScenarioRequest request);
    List<ApiScenarioWithBLOBs> selectByTagId(@Param("id") String id);

    List<ApiScenarioWithBLOBs> selectIds(@Param("ids") List<String> ids);

    List<ApiScenarioWithBLOBs> selectByIds(@Param("ids") String ids,@Param("oderId") String oderId);

    List<ApiScenario> selectReference(@Param("request") ApiScenarioRequest request);

    int removeToGc(@Param("ids") List<String> ids);


    int removeToGcByExample(ApiScenarioExample example);

    int reduction(@Param("ids") List<String> ids);

    long countByProjectID(String projectId);

    List<ApiScenarioWithBLOBs> selectIdAndScenarioByProjectId(String projectId);

    long countByProjectIDAndCreatInThisWeek(@Param("projectId") String projectId, @Param("firstDayTimestamp") long firstDayTimestamp, @Param("lastDayTimestamp") long lastDayTimestamp);

    List<ApiDataCountResult> countRunResultByProjectID(String projectId);

    List<String> selectIdsNotExistsInPlan(String projectId, String planId);

    ApiScenario getNextNum(@Param("projectId") String projectId);

    List<String> selectIdsByQuery(@Param("request") ApiScenarioRequest request);

    List<ApiDataCountResult> countModuleByProjectId(String projectId);
}
