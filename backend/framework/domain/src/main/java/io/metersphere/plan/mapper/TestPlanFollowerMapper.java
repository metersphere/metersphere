package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanFollower;
import io.metersphere.plan.domain.TestPlanFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanFollowerMapper {
    long countByExample(TestPlanFollowerExample example);

    int deleteByExample(TestPlanFollowerExample example);

    int deleteByPrimaryKey(@Param("testPlanId") String testPlanId, @Param("userId") String userId);

    int insert(TestPlanFollower record);

    int insertSelective(TestPlanFollower record);

    List<TestPlanFollower> selectByExample(TestPlanFollowerExample example);

    int updateByExampleSelective(@Param("record") TestPlanFollower record, @Param("example") TestPlanFollowerExample example);

    int updateByExample(@Param("record") TestPlanFollower record, @Param("example") TestPlanFollowerExample example);

    int batchInsert(@Param("list") List<TestPlanFollower> list);

    int batchInsertSelective(@Param("list") List<TestPlanFollower> list, @Param("selective") TestPlanFollower.Column ... selective);
}