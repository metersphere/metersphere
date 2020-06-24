package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiTest;
import io.metersphere.base.domain.ApiTestExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestMapper {
    long countByExample(ApiTestExample example);

    int deleteByExample(ApiTestExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTest record);

    int insertSelective(ApiTest record);

    List<ApiTest> selectByExampleWithBLOBs(ApiTestExample example);

    List<ApiTest> selectByExample(ApiTestExample example);

    ApiTest selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTest record, @Param("example") ApiTestExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTest record, @Param("example") ApiTestExample example);

    int updateByExample(@Param("record") ApiTest record, @Param("example") ApiTestExample example);

    int updateByPrimaryKeySelective(ApiTest record);

    int updateByPrimaryKeyWithBLOBs(ApiTest record);

    int updateByPrimaryKey(ApiTest record);
}