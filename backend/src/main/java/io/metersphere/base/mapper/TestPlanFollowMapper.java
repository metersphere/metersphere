package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanFollow;
import io.metersphere.base.domain.TestPlanFollowExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface TestPlanFollowMapper {
    long countByExample(TestPlanFollowExample example);

    int deleteByExample(TestPlanFollowExample example);

    int insert(TestPlanFollow record);

    int insertSelective(TestPlanFollow record);

    List<TestPlanFollow> selectByExample(TestPlanFollowExample example);

    int updateByExampleSelective(@Param("record") TestPlanFollow record, @Param("example") TestPlanFollowExample example);

    int updateByExample(@Param("record") TestPlanFollow record, @Param("example") TestPlanFollowExample example);
}