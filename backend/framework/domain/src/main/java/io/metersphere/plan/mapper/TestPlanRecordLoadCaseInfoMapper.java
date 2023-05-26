package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanRecordLoadCaseInfo;
import io.metersphere.plan.domain.TestPlanRecordLoadCaseInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanRecordLoadCaseInfoMapper {
    long countByExample(TestPlanRecordLoadCaseInfoExample example);

    int deleteByExample(TestPlanRecordLoadCaseInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanRecordLoadCaseInfo record);

    int insertSelective(TestPlanRecordLoadCaseInfo record);

    List<TestPlanRecordLoadCaseInfo> selectByExample(TestPlanRecordLoadCaseInfoExample example);

    TestPlanRecordLoadCaseInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanRecordLoadCaseInfo record, @Param("example") TestPlanRecordLoadCaseInfoExample example);

    int updateByExample(@Param("record") TestPlanRecordLoadCaseInfo record, @Param("example") TestPlanRecordLoadCaseInfoExample example);

    int updateByPrimaryKeySelective(TestPlanRecordLoadCaseInfo record);

    int updateByPrimaryKey(TestPlanRecordLoadCaseInfo record);
}