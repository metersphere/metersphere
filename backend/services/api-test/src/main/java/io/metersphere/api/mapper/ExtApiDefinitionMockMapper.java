package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiMockWithBlob;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMockMapper {

    List<ApiDefinitionMockDTO> list(@Param("request") ApiDefinitionMockPageRequest request);

    List<String> getIdsByApiIds(@Param("ids") List<String> ids);

    List<String> getIds(@Param("request")ApiTestCaseBatchRequest request);
    List<ApiDefinitionMock> getTagsByIds(@Param("ids") List<String> ids);

    List<ApiDefinitionMock> getMockInfoByIds(@Param("ids") List<String> ids);

    List<ApiMockWithBlob> selectAllDetailByApiIds(@Param("apiIds") List<String> apiIds);
}
