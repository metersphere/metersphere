package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseTest;
import io.metersphere.functional.domain.FunctionalCaseTestExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseTestMapper {
    long countByExample(FunctionalCaseTestExample example);

    int deleteByExample(FunctionalCaseTestExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseTest record);

    int insertSelective(FunctionalCaseTest record);

    List<FunctionalCaseTest> selectByExample(FunctionalCaseTestExample example);

    FunctionalCaseTest selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseTest record, @Param("example") FunctionalCaseTestExample example);

    int updateByExample(@Param("record") FunctionalCaseTest record, @Param("example") FunctionalCaseTestExample example);

    int updateByPrimaryKeySelective(FunctionalCaseTest record);

    int updateByPrimaryKey(FunctionalCaseTest record);

    int batchInsert(@Param("list") List<FunctionalCaseTest> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseTest> list, @Param("selective") FunctionalCaseTest.Column ... selective);
}