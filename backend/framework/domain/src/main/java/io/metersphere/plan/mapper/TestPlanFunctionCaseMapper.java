package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanFunctionCase;
import io.metersphere.plan.domain.TestPlanFunctionCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanFunctionCaseMapper {
    long countByExample(TestPlanFunctionCaseExample example);

    int deleteByExample(TestPlanFunctionCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanFunctionCase record);

    int insertSelective(TestPlanFunctionCase record);

    List<TestPlanFunctionCase> selectByExample(TestPlanFunctionCaseExample example);

    TestPlanFunctionCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanFunctionCase record, @Param("example") TestPlanFunctionCaseExample example);

    int updateByExample(@Param("record") TestPlanFunctionCase record, @Param("example") TestPlanFunctionCaseExample example);

    int updateByPrimaryKeySelective(TestPlanFunctionCase record);

    int updateByPrimaryKey(TestPlanFunctionCase record);
}