package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiExecutionQueueMapper {
    long countByExample(ApiExecutionQueueExample example);

    int deleteByExample(ApiExecutionQueueExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiExecutionQueue record);

    int insertSelective(ApiExecutionQueue record);

    List<ApiExecutionQueue> selectByExample(ApiExecutionQueueExample example);

    ApiExecutionQueue selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiExecutionQueue record, @Param("example") ApiExecutionQueueExample example);

    int updateByExample(@Param("record") ApiExecutionQueue record, @Param("example") ApiExecutionQueueExample example);

    int updateByPrimaryKeySelective(ApiExecutionQueue record);

    int updateByPrimaryKey(ApiExecutionQueue record);
}