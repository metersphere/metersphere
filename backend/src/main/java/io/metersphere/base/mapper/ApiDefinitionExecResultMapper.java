package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDefinitionExecResult;

public interface ApiDefinitionExecResultMapper {

    int deleteByResourceId(String id);

    int insert(ApiDefinitionExecResult record);

    ApiDefinitionExecResult selectByResourceId(String resourceId);

    ApiDefinitionExecResult selectByPrimaryKey(String id);

}