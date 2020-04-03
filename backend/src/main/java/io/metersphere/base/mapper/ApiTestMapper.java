package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiTest;
import io.metersphere.base.domain.ApiTestExample;
import io.metersphere.base.domain.ApiTestWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestMapper {
    long countByExample(ApiTestExample example);

    int deleteByExample(ApiTestExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestWithBLOBs record);

    int insertSelective(ApiTestWithBLOBs record);

    List<ApiTestWithBLOBs> selectByExampleWithBLOBs(ApiTestExample example);

    List<ApiTest> selectByExample(ApiTestExample example);

    ApiTestWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestWithBLOBs record, @Param("example") ApiTestExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTestWithBLOBs record, @Param("example") ApiTestExample example);

    int updateByExample(@Param("record") ApiTest record, @Param("example") ApiTestExample example);

    int updateByPrimaryKeySelective(ApiTestWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiTestWithBLOBs record);

    int updateByPrimaryKey(ApiTest record);
}