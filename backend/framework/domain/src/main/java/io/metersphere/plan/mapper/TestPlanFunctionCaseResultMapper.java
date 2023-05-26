package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanFunctionCaseResult;
import io.metersphere.plan.domain.TestPlanFunctionCaseResultExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanFunctionCaseResultMapper {
    long countByExample(TestPlanFunctionCaseResultExample example);

    int deleteByExample(TestPlanFunctionCaseResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanFunctionCaseResult record);

    int insertSelective(TestPlanFunctionCaseResult record);

    List<TestPlanFunctionCaseResult> selectByExample(TestPlanFunctionCaseResultExample example);

    TestPlanFunctionCaseResult selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanFunctionCaseResult record, @Param("example") TestPlanFunctionCaseResultExample example);

    int updateByExample(@Param("record") TestPlanFunctionCaseResult record, @Param("example") TestPlanFunctionCaseResultExample example);

    int updateByPrimaryKeySelective(TestPlanFunctionCaseResult record);

    int updateByPrimaryKey(TestPlanFunctionCaseResult record);
}