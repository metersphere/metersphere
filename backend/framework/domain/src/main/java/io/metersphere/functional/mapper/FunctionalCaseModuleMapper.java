package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.functional.domain.FunctionalCaseModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseModuleMapper {
    long countByExample(FunctionalCaseModuleExample example);

    int deleteByExample(FunctionalCaseModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseModule record);

    int insertSelective(FunctionalCaseModule record);

    List<FunctionalCaseModule> selectByExample(FunctionalCaseModuleExample example);

    FunctionalCaseModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseModule record, @Param("example") FunctionalCaseModuleExample example);

    int updateByExample(@Param("record") FunctionalCaseModule record, @Param("example") FunctionalCaseModuleExample example);

    int updateByPrimaryKeySelective(FunctionalCaseModule record);

    int updateByPrimaryKey(FunctionalCaseModule record);

    int batchInsert(@Param("list") List<FunctionalCaseModule> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseModule> list, @Param("selective") FunctionalCaseModule.Column ... selective);
}