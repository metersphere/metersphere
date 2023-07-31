package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioFollower;
import io.metersphere.ui.domain.UiScenarioFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioFollowerMapper {
    long countByExample(UiScenarioFollowerExample example);

    int deleteByExample(UiScenarioFollowerExample example);

    int deleteByPrimaryKey(@Param("scenarioId") String scenarioId, @Param("userId") String userId);

    int insert(UiScenarioFollower record);

    int insertSelective(UiScenarioFollower record);

    List<UiScenarioFollower> selectByExample(UiScenarioFollowerExample example);

    int updateByExampleSelective(@Param("record") UiScenarioFollower record, @Param("example") UiScenarioFollowerExample example);

    int updateByExample(@Param("record") UiScenarioFollower record, @Param("example") UiScenarioFollowerExample example);

    int batchInsert(@Param("list") List<UiScenarioFollower> list);

    int batchInsertSelective(@Param("list") List<UiScenarioFollower> list, @Param("selective") UiScenarioFollower.Column ... selective);
}