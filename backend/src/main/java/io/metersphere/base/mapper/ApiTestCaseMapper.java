package io.metersphere.base.mapper;

import io.metersphere.api.dto.delimit.ApiTestCaseRequest;
import io.metersphere.api.dto.delimit.ApiTestCaseResult;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestCaseExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiTestCaseMapper {
    
    List<ApiTestCaseResult> list(@Param("request") ApiTestCaseRequest request);

    long countByExample(ApiTestCaseExample example);

    int deleteByExample(ApiTestCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestCase record);

    List<ApiTestCase> selectByExampleWithBLOBs(ApiTestCaseExample example);

    List<ApiTestCase> selectByExample(ApiTestCaseExample example);

    ApiTestCase selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ApiTestCase record);

    int updateByPrimaryKey(ApiTestCase record);
}