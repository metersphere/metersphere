package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.domain.TestPlanLoadCaseExample;
import io.metersphere.base.domain.TestPlanLoadCaseWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanLoadCaseMapper {
    long countByExample(TestPlanLoadCaseExample example);

    int deleteByExample(TestPlanLoadCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanLoadCaseWithBLOBs record);

    int insertSelective(TestPlanLoadCaseWithBLOBs record);

    List<TestPlanLoadCaseWithBLOBs> selectByExampleWithBLOBs(TestPlanLoadCaseExample example);

    List<TestPlanLoadCase> selectByExample(TestPlanLoadCaseExample example);

    TestPlanLoadCaseWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanLoadCaseWithBLOBs record, @Param("example") TestPlanLoadCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanLoadCaseWithBLOBs record, @Param("example") TestPlanLoadCaseExample example);

    int updateByExample(@Param("record") TestPlanLoadCase record, @Param("example") TestPlanLoadCaseExample example);

    int updateByPrimaryKeySelective(TestPlanLoadCaseWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TestPlanLoadCaseWithBLOBs record);

    int updateByPrimaryKey(TestPlanLoadCase record);
}