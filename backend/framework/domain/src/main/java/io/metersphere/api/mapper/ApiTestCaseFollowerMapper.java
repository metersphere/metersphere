package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiTestCaseFollower;
import io.metersphere.api.domain.ApiTestCaseFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestCaseFollowerMapper {
    long countByExample(ApiTestCaseFollowerExample example);

    int deleteByExample(ApiTestCaseFollowerExample example);

    int deleteByPrimaryKey(@Param("caseId") String caseId, @Param("userId") String userId);

    int insert(ApiTestCaseFollower record);

    int insertSelective(ApiTestCaseFollower record);

    List<ApiTestCaseFollower> selectByExample(ApiTestCaseFollowerExample example);

    int updateByExampleSelective(@Param("record") ApiTestCaseFollower record, @Param("example") ApiTestCaseFollowerExample example);

    int updateByExample(@Param("record") ApiTestCaseFollower record, @Param("example") ApiTestCaseFollowerExample example);

    int batchInsert(@Param("list") List<ApiTestCaseFollower> list);

    int batchInsertSelective(@Param("list") List<ApiTestCaseFollower> list, @Param("selective") ApiTestCaseFollower.Column ... selective);
}