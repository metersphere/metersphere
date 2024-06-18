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
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioPageRequest request, @Param("isRepeat") boolean isRepeat, @Param("testPlanId") String testPlanId);

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

    /**
     * 获取缺陷未关联的场景用例列表
     *
     * @param request provider参数
     * @param deleted 是否删除状态
     * @param sort    排序
     * @return 通用的列表Case集合
     */
    List<TestCaseProviderDTO> listUnRelatedCaseWithBug(@Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted, @Param("sort") String sort);

    /**
     * 根据关联条件获取关联的用例ID
     *
     * @param request 关联参数
     * @param deleted 是否删除状态
     * @return 关联的用例ID集合
     */
    List<String> getSelectIdsByAssociateParam(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);

    ApiScenario getScenarioByResourceId(String id);

    ApiScenario getScenarioByReportId(String reportId);
}
