package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.dto.scenario.ApiScenarioBatchEditRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.scenario.ApiScenarioPageRequest;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioPageRequest request);

    List<String> getIds(@Param("request") ApiScenarioBatchEditRequest request, @Param("deleted") boolean deleted);

    List<ApiScenario> getInfoByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiScenario> getTagsByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<TestCaseProviderDTO> listByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<ModuleCountDTO> countModuleIdByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(@Param("projectId") String projectId);

    List<ApiScenario> getTestCaseByProvider(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);

    Long getLastPos(@Param("projectId") String projectId);

    List<TaskCenterDTO> taskCenterlist(@Param("request") TaskCenterPageRequest request, @Param("projectIds") List<String> projectIds);
}
