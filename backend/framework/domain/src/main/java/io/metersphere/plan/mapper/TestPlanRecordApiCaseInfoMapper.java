package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanRecordApiCaseInfo;
import io.metersphere.plan.domain.TestPlanRecordApiCaseInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanRecordApiCaseInfoMapper {
    long countByExample(TestPlanRecordApiCaseInfoExample example);

    int deleteByExample(TestPlanRecordApiCaseInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanRecordApiCaseInfo record);

    int insertSelective(TestPlanRecordApiCaseInfo record);

    List<TestPlanRecordApiCaseInfo> selectByExample(TestPlanRecordApiCaseInfoExample example);

    TestPlanRecordApiCaseInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanRecordApiCaseInfo record, @Param("example") TestPlanRecordApiCaseInfoExample example);

    int updateByExample(@Param("record") TestPlanRecordApiCaseInfo record, @Param("example") TestPlanRecordApiCaseInfoExample example);

    int updateByPrimaryKeySelective(TestPlanRecordApiCaseInfo record);

    int updateByPrimaryKey(TestPlanRecordApiCaseInfo record);
}