package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestCaseExample;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestCaseMapper {
    long countByExample(ApiTestCaseExample example);

    int deleteByExample(ApiTestCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestCaseWithBLOBs record);

    int insertSelective(ApiTestCaseWithBLOBs record);

    List<ApiTestCaseWithBLOBs> selectByExampleWithBLOBs(ApiTestCaseExample example);

    List<ApiTestCase> selectByExample(ApiTestCaseExample example);

    ApiTestCaseWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestCaseWithBLOBs record, @Param("example") ApiTestCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTestCaseWithBLOBs record, @Param("example") ApiTestCaseExample example);

    int updateByExample(@Param("record") ApiTestCase record, @Param("example") ApiTestCaseExample example);

    int updateByPrimaryKeySelective(ApiTestCaseWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiTestCaseWithBLOBs record);

    int updateByPrimaryKey(ApiTestCase record);
}