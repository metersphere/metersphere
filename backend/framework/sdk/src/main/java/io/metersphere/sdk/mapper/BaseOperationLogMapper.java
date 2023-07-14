package io.metersphere.sdk.mapper;


import io.metersphere.sdk.log.vo.OperationLogRequest;
import io.metersphere.sdk.log.vo.OperationLogResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseOperationLogMapper {

    List<OperationLogResponse> list(@Param("request") OperationLogRequest request);


}