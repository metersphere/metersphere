package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.base.domain.TestPlanTestCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanTestCaseMapper {
    long countByExample(TestPlanTestCaseExample example);

    int deleteByExample(TestPlanTestCaseExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TestPlanTestCase record);

    int insertSelective(TestPlanTestCase record);

    List<TestPlanTestCase> selectByExampleWithBLOBs(TestPlanTestCaseExample example);

    List<TestPlanTestCase> selectByExample(TestPlanTestCaseExample example);

    TestPlanTestCase selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TestPlanTestCase record, @Param("example") TestPlanTestCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanTestCase record, @Param("example") TestPlanTestCaseExample example);

    int updateByExample(@Param("record") TestPlanTestCase record, @Param("example") TestPlanTestCaseExample example);

    int updateByPrimaryKeySelective(TestPlanTestCase record);

    int updateByPrimaryKeyWithBLOBs(TestPlanTestCase record);

    int updateByPrimaryKey(TestPlanTestCase record);
}