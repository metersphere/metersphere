package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDefinitionCustomField;
import io.metersphere.api.dto.definition.ApiDefinitionCustomFieldDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: LAN
 * @date: 2023/12/12 10:12
 * @version: 1.0
 */
public interface ExtApiDefinitionCustomFieldMapper {
    /**
     * 获取缺陷自定义字段值
     * @param apiIds 接口集合
     * @param projectId 项目ID
     * @return 缺陷自定义字段值
     */
    List<ApiDefinitionCustomFieldDTO> getApiCustomFields(@Param("ids") List<String> apiIds, @Param("projectId") String projectId);

    int batchInsertCustomField(@Param("apiId") String apiId, @Param("list") List<ApiDefinitionCustomField> list);

}
