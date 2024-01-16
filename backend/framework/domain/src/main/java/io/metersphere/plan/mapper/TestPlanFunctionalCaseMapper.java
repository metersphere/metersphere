package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanFunctionalCaseMapper {
    long countByExample(TestPlanFunctionalCaseExample example);

    int deleteByExample(TestPlanFunctionalCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanFunctionalCase record);

    int insertSelective(TestPlanFunctionalCase record);

    List<TestPlanFunctionalCase> selectByExample(TestPlanFunctionalCaseExample example);

    TestPlanFunctionalCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanFunctionalCase record, @Param("example") TestPlanFunctionalCaseExample example);

    int updateByExample(@Param("record") TestPlanFunctionalCase record, @Param("example") TestPlanFunctionalCaseExample example);

    int updateByPrimaryKeySelective(TestPlanFunctionalCase record);

    int updateByPrimaryKey(TestPlanFunctionalCase record);

    int batchInsert(@Param("list") List<TestPlanFunctionalCase> list);

    int batchInsertSelective(@Param("list") List<TestPlanFunctionalCase> list, @Param("selective") TestPlanFunctionalCase.Column ... selective);
}