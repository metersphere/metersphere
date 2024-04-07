package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.dto.definition.ExecutePageRequest;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioPageRequest request);

    List<String> getIds(@Param("request") ApiScenarioBatchRequest request, @Param("deleted") boolean deleted);

    List<ApiScenario> getInfoByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiScenario> getTagsByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<TestCaseProviderDTO> listByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<ModuleCountDTO> countModuleIdByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(@Param("projectId") String projectId);

    List<ApiScenario> getTestCaseByProvider(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);

    Long getLastPos(@Param("projectId") String projectId);

    List<ApiScenarioAssociationDTO> getAssociationPage(@Param("request") ApiScenarioAssociationPageRequest request);

    List<String> getIdsByModules(@Param("request") ScenarioSystemRequest scenarioRequest);

    List<String> selectByProjectId(String projectId);

    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    Long getLastPosEdit(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    List<ExecuteReportDTO> getExecuteList(@Param("request") ExecutePageRequest request);

    List<String> selectIdByProjectIdOrderByPos(String projectId);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    void updatePos(String id, long pos);

    Long getPos(String projectId);

    List<ApiScenario> getScenarioExecuteInfoByIds(@Param("ids") List<String> ids);

    List<ModuleCountDTO> countModuleIdByRequest(@Param("request") ApiScenarioModuleRequest request, @Param("deleted") boolean deleted);
}
