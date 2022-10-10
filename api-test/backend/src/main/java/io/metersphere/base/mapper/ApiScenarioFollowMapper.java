package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiScenarioFollow;
import io.metersphere.base.domain.ApiScenarioFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioFollowMapper {
    long countByExample(ApiScenarioFollowExample example);

    int deleteByExample(ApiScenarioFollowExample example);

    int insert(ApiScenarioFollow record);

    int insertSelective(ApiScenarioFollow record);

    List<ApiScenarioFollow> selectByExample(ApiScenarioFollowExample example);

    int updateByExampleSelective(@Param("record") ApiScenarioFollow record, @Param("example") ApiScenarioFollowExample example);

    int updateByExample(@Param("record") ApiScenarioFollow record, @Param("example") ApiScenarioFollowExample example);
}