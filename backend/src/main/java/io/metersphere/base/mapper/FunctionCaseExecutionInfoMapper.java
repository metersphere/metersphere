package io.metersphere.base.mapper;

import io.metersphere.base.domain.FunctionCaseExecutionInfo;
import io.metersphere.base.domain.FunctionCaseExecutionInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionCaseExecutionInfoMapper {
    long countByExample(FunctionCaseExecutionInfoExample example);

    int deleteByExample(FunctionCaseExecutionInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionCaseExecutionInfo record);

    int insertSelective(FunctionCaseExecutionInfo record);

    List<FunctionCaseExecutionInfo> selectByExample(FunctionCaseExecutionInfoExample example);

    FunctionCaseExecutionInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionCaseExecutionInfo record, @Param("example") FunctionCaseExecutionInfoExample example);

    int updateByExample(@Param("record") FunctionCaseExecutionInfo record, @Param("example") FunctionCaseExecutionInfoExample example);

    int updateByPrimaryKeySelective(FunctionCaseExecutionInfo record);

    int updateByPrimaryKey(FunctionCaseExecutionInfo record);
}