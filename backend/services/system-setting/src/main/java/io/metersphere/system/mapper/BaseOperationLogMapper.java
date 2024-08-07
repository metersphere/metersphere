package io.metersphere.system.mapper;


import io.metersphere.system.log.vo.OperationLogResponse;
import io.metersphere.system.log.vo.SystemOperationLogRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseOperationLogMapper {

    List<OperationLogResponse> list(@Param("request") SystemOperationLogRequest request);

    void deleteByTime(@Param("timestamp") long timestamp);

    List<Long> selectIdByHistoryIds(@Param("ids") List<Long> ids);
}