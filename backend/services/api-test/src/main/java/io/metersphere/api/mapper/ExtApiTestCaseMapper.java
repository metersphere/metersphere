package io.metersphere.api.mapper;


import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCasePageRequest;
import io.metersphere.api.dto.definition.CasePassDTO;
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


    void batchMoveGc(@Param("ids") List<String> ids, @Param("userId") String userId);

    List<ApiTestCase> getCaseInfoByApiIds(@Param("ids") List<String> apiIds, @Param("deleted") boolean deleted);

    List<ApiTestCase> getCaseInfoByIds(@Param("ids") List<String> caseIds, @Param("deleted") boolean deleted);

    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    List<CasePassDTO> findPassRateByIds(@Param("ids") List<String> ids);
}