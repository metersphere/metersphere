package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioFollower;
import io.metersphere.api.domain.ApiScenarioFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioFollowerMapper {
    long countByExample(ApiScenarioFollowerExample example);

    int deleteByExample(ApiScenarioFollowerExample example);

    int deleteByPrimaryKey(@Param("apiScenarioId") String apiScenarioId, @Param("userId") String userId);

    int insert(ApiScenarioFollower record);

    int insertSelective(ApiScenarioFollower record);

    List<ApiScenarioFollower> selectByExample(ApiScenarioFollowerExample example);

    int updateByExampleSelective(@Param("record") ApiScenarioFollower record, @Param("example") ApiScenarioFollowerExample example);

    int updateByExample(@Param("record") ApiScenarioFollower record, @Param("example") ApiScenarioFollowerExample example);

    int batchInsert(@Param("list") List<ApiScenarioFollower> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioFollower> list, @Param("selective") ApiScenarioFollower.Column ... selective);
}