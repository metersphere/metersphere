package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiExecutionInfo;
import io.metersphere.base.domain.ApiExecutionInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiExecutionInfoMapper {
    long countByExample(ApiExecutionInfoExample example);

    int deleteByExample(ApiExecutionInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiExecutionInfo record);

    int insertSelective(ApiExecutionInfo record);

    List<ApiExecutionInfo> selectByExample(ApiExecutionInfoExample example);

    ApiExecutionInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiExecutionInfo record, @Param("example") ApiExecutionInfoExample example);

    int updateByExample(@Param("record") ApiExecutionInfo record, @Param("example") ApiExecutionInfoExample example);

    int updateByPrimaryKeySelective(ApiExecutionInfo record);

    int updateByPrimaryKey(ApiExecutionInfo record);
}