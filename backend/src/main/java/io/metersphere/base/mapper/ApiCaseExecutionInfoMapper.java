package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiCaseExecutionInfo;
import io.metersphere.base.domain.ApiCaseExecutionInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiCaseExecutionInfoMapper {
    long countByExample(ApiCaseExecutionInfoExample example);

    int deleteByExample(ApiCaseExecutionInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiCaseExecutionInfo record);

    int insertSelective(ApiCaseExecutionInfo record);

    List<ApiCaseExecutionInfo> selectByExample(ApiCaseExecutionInfoExample example);

    ApiCaseExecutionInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiCaseExecutionInfo record, @Param("example") ApiCaseExecutionInfoExample example);

    int updateByExample(@Param("record") ApiCaseExecutionInfo record, @Param("example") ApiCaseExecutionInfoExample example);

    int updateByPrimaryKeySelective(ApiCaseExecutionInfo record);

    int updateByPrimaryKey(ApiCaseExecutionInfo record);
}