package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiMockWithBlob;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMockMapper {

    @BaseConditionFilter
    List<ApiDefinitionMockDTO> list(@Param("request") ApiDefinitionMockPageRequest request);

    List<String> getIdsByApiIds(@Param("ids") List<String> ids);

    @BaseConditionFilter
    List<String> getIds(@Param("request") ApiTestCaseBatchRequest request);

    List<ApiDefinitionMock> getTagsByIds(@Param("ids") List<String> ids);

    List<ApiDefinitionMock> getMockInfoByIds(@Param("ids") List<String> ids);

    List<ApiMockWithBlob> selectAllDetailByApiIds(@Param("apiIds") List<String> apiIds);
}
