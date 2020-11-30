package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDefinitionHistory;

import java.util.List;

public interface ApiDefinitionHistoryMapper {

    int deleteByApiDefinitionId(String id);

    int insert(ApiDefinitionHistory record);

    List<ApiDefinitionHistory> selectByApiDefinitionId(String id);

}