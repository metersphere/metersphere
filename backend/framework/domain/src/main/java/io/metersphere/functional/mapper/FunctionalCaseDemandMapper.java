package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.domain.FunctionalCaseDemandExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseDemandMapper {
    long countByExample(FunctionalCaseDemandExample example);

    int deleteByExample(FunctionalCaseDemandExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseDemand record);

    int insertSelective(FunctionalCaseDemand record);

    List<FunctionalCaseDemand> selectByExample(FunctionalCaseDemandExample example);

    FunctionalCaseDemand selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseDemand record, @Param("example") FunctionalCaseDemandExample example);

    int updateByExample(@Param("record") FunctionalCaseDemand record, @Param("example") FunctionalCaseDemandExample example);

    int updateByPrimaryKeySelective(FunctionalCaseDemand record);

    int updateByPrimaryKey(FunctionalCaseDemand record);

    int batchInsert(@Param("list") List<FunctionalCaseDemand> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseDemand> list, @Param("selective") FunctionalCaseDemand.Column ... selective);
}