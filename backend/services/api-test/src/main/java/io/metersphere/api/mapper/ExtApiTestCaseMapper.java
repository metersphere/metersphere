package io.metersphere.api.mapper;


import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.scenario.ScenarioSystemRequest;
import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Mapper
public interface ExtApiTestCaseMapper {

    Long getPos(@Param("projectId") String projectId);

    List<ApiTestCaseDTO> listByRequest(@Param("request") ApiTestCasePageRequest request, @Param("deleted") boolean deleted);

    List<String> getIds(@Param("request") ApiTestCaseBatchRequest request, @Param("deleted") boolean deleted);

    void batchMoveGc(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("deleteTime") long deleteTime);

    List<ApiTestCase> getCaseInfoByApiIds(@Param("ids") List<String> apiIds, @Param("deleted") boolean deleted);

    List<ApiTestCase> getCaseInfoByIds(@Param("ids") List<String> caseIds, @Param("deleted") boolean deleted);

    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    List<CasePassDTO> findPassRateByIds(@Param("ids") List<String> ids);

    List<String> selectIdsByCaseIds(@Param("ids") List<String> ids);

    List<String> getCaseIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<String> getIdsByApiIds(@Param("ids") List<String> ids);

    List<TestCaseProviderDTO> listByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<ModuleCountDTO> countModuleIdByProviderRequest(@Param("table") String resourceType, @Param("sourceName") String sourceName, @Param("apiCaseColumnName") String apiCaseColumnName, @Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(@Param("projectId") String projectId);

    List<ApiTestCase> getTestCaseByProvider(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);


    List<ApiTestCase> getTagsByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiCaseReportDTO> getExecuteList(@Param("request") ApiCaseExecutePageRequest request);

    List<OptionDTO> selectVersionOptionByIds(@Param("ids") List<String> ids);

    List<String> getIdsByModules(@Param("request") ScenarioSystemRequest caseRequest);
}