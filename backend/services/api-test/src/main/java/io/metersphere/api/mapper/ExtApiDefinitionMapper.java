package io.metersphere.api.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMapper {
    void deleteApiToGc(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("time") long time);
}
