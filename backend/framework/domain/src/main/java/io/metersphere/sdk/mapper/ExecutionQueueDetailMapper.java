package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.ExecutionQueueDetail;
import io.metersphere.sdk.domain.ExecutionQueueDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExecutionQueueDetailMapper {
    long countByExample(ExecutionQueueDetailExample example);

    int deleteByExample(ExecutionQueueDetailExample example);

    int deleteByPrimaryKey(String id);

    int insert(ExecutionQueueDetail record);

    int insertSelective(ExecutionQueueDetail record);

    List<ExecutionQueueDetail> selectByExampleWithBLOBs(ExecutionQueueDetailExample example);

    List<ExecutionQueueDetail> selectByExample(ExecutionQueueDetailExample example);

    ExecutionQueueDetail selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ExecutionQueueDetail record, @Param("example") ExecutionQueueDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") ExecutionQueueDetail record, @Param("example") ExecutionQueueDetailExample example);

    int updateByExample(@Param("record") ExecutionQueueDetail record, @Param("example") ExecutionQueueDetailExample example);

    int updateByPrimaryKeySelective(ExecutionQueueDetail record);

    int updateByPrimaryKeyWithBLOBs(ExecutionQueueDetail record);

    int updateByPrimaryKey(ExecutionQueueDetail record);
}