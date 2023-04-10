package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.ScheduleDTO;
import io.metersphere.api.dto.definition.ApiSwaggerUrlDTO;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.request.QueryScheduleRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtScheduleMapper {
    List<ScheduleDao> list(@Param("request") QueryScheduleRequest request);

    void insert(@Param("apiSwaggerUrlDTO") ApiSwaggerUrlDTO apiSwaggerUrlDTO);

    ApiSwaggerUrlDTO select(String id);

    int updateNameByResourceID(@Param("resourceId") String resourceId, @Param("name") String name);

    List<ScheduleDTO> selectByResourceIds(@Param("ids") List<String> resourceIDs);

}
