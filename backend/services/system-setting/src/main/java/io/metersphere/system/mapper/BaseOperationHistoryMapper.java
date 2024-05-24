package io.metersphere.system.mapper;


import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseOperationHistoryMapper {

    List<String> selectSourceIds(@Param("limit") int limit);

    List<Long> selectIdsBySourceId(@Param("sourceId") String sourceId, @Param("limit") int limit);

    void deleteBySourceId(@Param("sourceId") String sourceId, @Param("ids") List<Long> ids);

    List<OperationHistoryDTO> list(@Param("request") OperationHistoryRequest request);

    List<OperationHistoryDTO> listWidthTable(@Param("request") OperationHistoryRequest request, @Param("table") String table);

    Long selectLatestIdByOperationId(String bugId);
}