package io.metersphere.base.mapper;


import io.metersphere.base.domain.TestCaseTest;
import io.metersphere.base.domain.TestCaseTestExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestCaseTestMapper {
    long countByExample(TestCaseTestExample example);

    int deleteByExample(TestCaseTestExample example);

    int insert(TestCaseTest record);

    int insertSelective(TestCaseTest record);

    List<TestCaseTest> selectByExample(TestCaseTestExample example);

    int updateByExampleSelective(@Param("record") TestCaseTest record, @Param("example") TestCaseTestExample example);

    int updateByExample(@Param("record") TestCaseTest record, @Param("example") TestCaseTestExample example);
}
