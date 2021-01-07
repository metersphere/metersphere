package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.domain.TestPlanLoadCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanLoadCaseMapper {
    long countByExample(TestPlanLoadCaseExample example);

    int deleteByExample(TestPlanLoadCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanLoadCase record);

    int insertSelective(TestPlanLoadCase record);

    List<TestPlanLoadCase> selectByExample(TestPlanLoadCaseExample example);

    TestPlanLoadCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanLoadCase record, @Param("example") TestPlanLoadCaseExample example);

    int updateByExample(@Param("record") TestPlanLoadCase record, @Param("example") TestPlanLoadCaseExample example);

    int updateByPrimaryKeySelective(TestPlanLoadCase record);

    int updateByPrimaryKey(TestPlanLoadCase record);
}