package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.metersphere.plan.domain.TestPlanCaseExecuteHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanCaseExecuteHistoryMapper {
    long countByExample(TestPlanCaseExecuteHistoryExample example);

    int deleteByExample(TestPlanCaseExecuteHistoryExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanCaseExecuteHistory record);

    int insertSelective(TestPlanCaseExecuteHistory record);

    List<TestPlanCaseExecuteHistory> selectByExampleWithBLOBs(TestPlanCaseExecuteHistoryExample example);

    List<TestPlanCaseExecuteHistory> selectByExample(TestPlanCaseExecuteHistoryExample example);

    TestPlanCaseExecuteHistory selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanCaseExecuteHistory record, @Param("example") TestPlanCaseExecuteHistoryExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanCaseExecuteHistory record, @Param("example") TestPlanCaseExecuteHistoryExample example);

    int updateByExample(@Param("record") TestPlanCaseExecuteHistory record, @Param("example") TestPlanCaseExecuteHistoryExample example);

    int updateByPrimaryKeySelective(TestPlanCaseExecuteHistory record);

    int updateByPrimaryKeyWithBLOBs(TestPlanCaseExecuteHistory record);

    int updateByPrimaryKey(TestPlanCaseExecuteHistory record);

    int batchInsert(@Param("list") List<TestPlanCaseExecuteHistory> list);

    int batchInsertSelective(@Param("list") List<TestPlanCaseExecuteHistory> list, @Param("selective") TestPlanCaseExecuteHistory.Column ... selective);
}