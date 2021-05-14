package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiEnvironmentRunningParam;
import io.metersphere.base.domain.ApiEnvironmentRunningParamExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiEnvironmentRunningParamMapper {
    long countByExample(ApiEnvironmentRunningParamExample example);

    int deleteByExample(ApiEnvironmentRunningParamExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiEnvironmentRunningParam record);

    int insertSelective(ApiEnvironmentRunningParam record);

    List<ApiEnvironmentRunningParam> selectByExampleWithBLOBs(ApiEnvironmentRunningParamExample example);

    List<ApiEnvironmentRunningParam> selectByExample(ApiEnvironmentRunningParamExample example);

    ApiEnvironmentRunningParam selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiEnvironmentRunningParam record, @Param("example") ApiEnvironmentRunningParamExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiEnvironmentRunningParam record, @Param("example") ApiEnvironmentRunningParamExample example);

    int updateByExample(@Param("record") ApiEnvironmentRunningParam record, @Param("example") ApiEnvironmentRunningParamExample example);

    int updateByPrimaryKeySelective(ApiEnvironmentRunningParam record);

    int updateByPrimaryKeyWithBLOBs(ApiEnvironmentRunningParam record);

    int updateByPrimaryKey(ApiEnvironmentRunningParam record);
}