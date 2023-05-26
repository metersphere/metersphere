package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanExecuteRecord;
import io.metersphere.plan.domain.TestPlanExecuteRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanExecuteRecordMapper {
    long countByExample(TestPlanExecuteRecordExample example);

    int deleteByExample(TestPlanExecuteRecordExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanExecuteRecord record);

    int insertSelective(TestPlanExecuteRecord record);

    List<TestPlanExecuteRecord> selectByExampleWithBLOBs(TestPlanExecuteRecordExample example);

    List<TestPlanExecuteRecord> selectByExample(TestPlanExecuteRecordExample example);

    TestPlanExecuteRecord selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanExecuteRecord record, @Param("example") TestPlanExecuteRecordExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanExecuteRecord record, @Param("example") TestPlanExecuteRecordExample example);

    int updateByExample(@Param("record") TestPlanExecuteRecord record, @Param("example") TestPlanExecuteRecordExample example);

    int updateByPrimaryKeySelective(TestPlanExecuteRecord record);

    int updateByPrimaryKeyWithBLOBs(TestPlanExecuteRecord record);

    int updateByPrimaryKey(TestPlanExecuteRecord record);
}