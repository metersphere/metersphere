package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiDelimitExecResult;

public interface ApiDelimitExecResultMapper {

    int deleteByResourceId(String id);

    int insert(ApiDelimitExecResult record);

    ApiDelimitExecResult selectByResourceId(String resourceId);

    ApiDelimitExecResult selectByPrimaryKey(String id);

}