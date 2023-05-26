package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiTestCaseFollow;
import io.metersphere.api.domain.ApiTestCaseFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiTestCaseFollowMapper {
    long countByExample(ApiTestCaseFollowExample example);

    int deleteByExample(ApiTestCaseFollowExample example);

    int deleteByPrimaryKey(@Param("caseId") String caseId, @Param("followId") String followId);

    int insert(ApiTestCaseFollow record);

    int insertSelective(ApiTestCaseFollow record);

    List<ApiTestCaseFollow> selectByExample(ApiTestCaseFollowExample example);

    int updateByExampleSelective(@Param("record") ApiTestCaseFollow record, @Param("example") ApiTestCaseFollowExample example);

    int updateByExample(@Param("record") ApiTestCaseFollow record, @Param("example") ApiTestCaseFollowExample example);
}