package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

public interface ExtUserViewMapper {
    Long getLastPos(@Param("scopeId") String scopeId, @Param("userId") String userId, @Param("viewType") String viewType, @Param("baseOrder") Long baseOrder);
}
