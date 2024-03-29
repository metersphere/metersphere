package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionCustomField;
import io.metersphere.api.dto.ApiDefinitionExecuteInfo;
import io.metersphere.api.dto.ReferenceDTO;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.converter.ApiDefinitionImportDetail;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.scenario.ScenarioSystemRequest;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMapper {
    void deleteApiToGc(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("time") long time);

    List<ApiDefinitionDTO> list(@Param("request") ApiDefinitionPageRequest request);

    List<ApiDefinitionDTO> listDoc(@Param("request") ApiDefinitionDocRequest request);

    List<ApiCaseComputeDTO> selectApiCaseByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids, @Param("projectId") String projectId);

    Long getPos(@Param("projectId") String projectId);

    List<String> getIds(@Param("request") TableBatchProcessDTO request, @Param("projectId") String projectId, @Param("protocol") String protocol, @Param("deleted") boolean deleted);

    List<String> getRefIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<String> getIdsByRefId(@Param("refIds") List<String> refIds, @Param("deleted") boolean deleted);

    List<ApiDefinition> getTagsByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiDefinitionVersionDTO> getApiDefinitionByRefId(@Param("refId") String refId);

    void batchMove(@Param("request") ApiDefinitionBatchMoveRequest request, @Param("ids") List<String> ids, @Param("userId") String userId);

    void batchDeleteByRefId(@Param("refIds") List<String> refIds, @Param("userId") String userId, @Param("projectId") String projectId);

    void batchDeleteById(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("projectId") String projectId);

    void batchRecoverById(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("projectId") String projectId);

    void clearLatestVersion(@Param("refId") String refId, @Param("projectId") String projectId);

    void updateLatestVersion(@Param("id") String id, @Param("projectId") String projectId);

    List<ApiDefinitionImportDetail> importList(@Param("request") ApiDefinitionPageRequest request);

    List<String> selectIdsByIdsAndDeleted(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<String> selectByProjectId(@Param("projectId") String projectId);

    List<OptionDTO> selectVersionOptionByIds(@Param("apiIds") List<String> apiIds);

    ApiDefinition selectApiDefinitionByVersion(@Param("refId") String refId, @Param("versionId") String versionId);

    List<ApiDefinitionCustomField> getCustomFieldByCaseIds(@Param("ids") List<String> ids);

    List<String> getIdsByModules(@Param("request") ScenarioSystemRequest request);

    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    List<ApiDefinitionExecuteInfo> getApiDefinitionExecuteInfo(@Param("ids") List<String> ids);

    ApiDefinition selectByProjectNumAndApiNum(@Param("projectNum") String projectNum, @Param("apiNum") String apiNum);

    void updatePos(String id, long pos);

    List<String> selectIdByProjectIdOrderByPos(String projectId);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<ReferenceDTO> getReference(@Param("request") ReferenceRequest request);

    List<ApiDefinition> selectByProjectNum(String projectNum);
}
