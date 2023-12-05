package io.metersphere.system.mapper;


import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseOperationHistoryMapper {

    List<String> selectSourceIds();

    List<Long> selectIdsBySourceId(@Param("sourceId") String sourceId, @Param("limit") int limit);

    void deleteByIds(@Param("sourceId") String sourceId, @Param("ids") List<Long> ids);
}