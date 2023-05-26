package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanConfigMapper {
    long countByExample(TestPlanConfigExample example);

    int deleteByExample(TestPlanConfigExample example);

    int deleteByPrimaryKey(String testPlanId);

    int insert(TestPlanConfig record);

    int insertSelective(TestPlanConfig record);

    List<TestPlanConfig> selectByExampleWithBLOBs(TestPlanConfigExample example);

    List<TestPlanConfig> selectByExample(TestPlanConfigExample example);

    TestPlanConfig selectByPrimaryKey(String testPlanId);

    int updateByExampleSelective(@Param("record") TestPlanConfig record, @Param("example") TestPlanConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanConfig record, @Param("example") TestPlanConfigExample example);

    int updateByExample(@Param("record") TestPlanConfig record, @Param("example") TestPlanConfigExample example);

    int updateByPrimaryKeySelective(TestPlanConfig record);

    int updateByPrimaryKeyWithBLOBs(TestPlanConfig record);

    int updateByPrimaryKey(TestPlanConfig record);
}