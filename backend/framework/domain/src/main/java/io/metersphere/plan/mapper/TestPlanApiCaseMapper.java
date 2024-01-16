package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanApiCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanApiCaseMapper {
    long countByExample(TestPlanApiCaseExample example);

    int deleteByExample(TestPlanApiCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanApiCase record);

    int insertSelective(TestPlanApiCase record);

    List<TestPlanApiCase> selectByExampleWithBLOBs(TestPlanApiCaseExample example);

    List<TestPlanApiCase> selectByExample(TestPlanApiCaseExample example);

    TestPlanApiCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanApiCase record, @Param("example") TestPlanApiCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanApiCase record, @Param("example") TestPlanApiCaseExample example);

    int updateByExample(@Param("record") TestPlanApiCase record, @Param("example") TestPlanApiCaseExample example);

    int updateByPrimaryKeySelective(TestPlanApiCase record);

    int updateByPrimaryKeyWithBLOBs(TestPlanApiCase record);

    int updateByPrimaryKey(TestPlanApiCase record);

    int batchInsert(@Param("list") List<TestPlanApiCase> list);

    int batchInsertSelective(@Param("list") List<TestPlanApiCase> list, @Param("selective") TestPlanApiCase.Column ... selective);
}