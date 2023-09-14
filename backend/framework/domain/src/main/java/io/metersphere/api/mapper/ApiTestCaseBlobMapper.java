package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiTestCaseBlob;
import io.metersphere.api.domain.ApiTestCaseBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestCaseBlobMapper {
    long countByExample(ApiTestCaseBlobExample example);

    int deleteByExample(ApiTestCaseBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestCaseBlob record);

    int insertSelective(ApiTestCaseBlob record);

    List<ApiTestCaseBlob> selectByExampleWithBLOBs(ApiTestCaseBlobExample example);

    List<ApiTestCaseBlob> selectByExample(ApiTestCaseBlobExample example);

    ApiTestCaseBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestCaseBlob record, @Param("example") ApiTestCaseBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiTestCaseBlob record, @Param("example") ApiTestCaseBlobExample example);

    int updateByExample(@Param("record") ApiTestCaseBlob record, @Param("example") ApiTestCaseBlobExample example);

    int updateByPrimaryKeySelective(ApiTestCaseBlob record);

    int updateByPrimaryKeyWithBLOBs(ApiTestCaseBlob record);

    int batchInsert(@Param("list") List<ApiTestCaseBlob> list);

    int batchInsertSelective(@Param("list") List<ApiTestCaseBlob> list, @Param("selective") ApiTestCaseBlob.Column ... selective);
}