package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.base.domain.ApiExecutionQueueDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiExecutionQueueDetailMapper {
    long countByExample(ApiExecutionQueueDetailExample example);

    int deleteByExample(ApiExecutionQueueDetailExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiExecutionQueueDetail record);

    int insertSelective(ApiExecutionQueueDetail record);

    List<ApiExecutionQueueDetail> selectByExampleWithBLOBs(ApiExecutionQueueDetailExample example);

    List<ApiExecutionQueueDetail> selectByExample(ApiExecutionQueueDetailExample example);

    ApiExecutionQueueDetail selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiExecutionQueueDetail record, @Param("example") ApiExecutionQueueDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiExecutionQueueDetail record, @Param("example") ApiExecutionQueueDetailExample example);

    int updateByExample(@Param("record") ApiExecutionQueueDetail record, @Param("example") ApiExecutionQueueDetailExample example);

    int updateByPrimaryKeySelective(ApiExecutionQueueDetail record);

    int updateByPrimaryKeyWithBLOBs(ApiExecutionQueueDetail record);

    int updateByPrimaryKey(ApiExecutionQueueDetail record);
}