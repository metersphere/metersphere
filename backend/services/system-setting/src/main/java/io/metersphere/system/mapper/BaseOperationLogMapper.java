package io.metersphere.system.mapper;


import io.metersphere.system.log.vo.OperationLogRequest;
import io.metersphere.system.log.vo.OperationLogResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseOperationLogMapper {

    List<OperationLogResponse> list(@Param("request") OperationLogRequest request);


}