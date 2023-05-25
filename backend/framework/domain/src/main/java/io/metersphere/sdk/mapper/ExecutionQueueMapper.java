package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.ExecutionQueue;
import io.metersphere.sdk.domain.ExecutionQueueExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExecutionQueueMapper {
    long countByExample(ExecutionQueueExample example);

    int deleteByExample(ExecutionQueueExample example);

    int deleteByPrimaryKey(String id);

    int insert(ExecutionQueue record);

    int insertSelective(ExecutionQueue record);

    List<ExecutionQueue> selectByExample(ExecutionQueueExample example);

    ExecutionQueue selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ExecutionQueue record, @Param("example") ExecutionQueueExample example);

    int updateByExample(@Param("record") ExecutionQueue record, @Param("example") ExecutionQueueExample example);

    int updateByPrimaryKeySelective(ExecutionQueue record);

    int updateByPrimaryKey(ExecutionQueue record);
}