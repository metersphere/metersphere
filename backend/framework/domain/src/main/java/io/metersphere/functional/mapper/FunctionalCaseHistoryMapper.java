package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseHistory;
import io.metersphere.functional.domain.FunctionalCaseHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseHistoryMapper {
    long countByExample(FunctionalCaseHistoryExample example);

    int deleteByExample(FunctionalCaseHistoryExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseHistory record);

    int insertSelective(FunctionalCaseHistory record);

    List<FunctionalCaseHistory> selectByExampleWithBLOBs(FunctionalCaseHistoryExample example);

    List<FunctionalCaseHistory> selectByExample(FunctionalCaseHistoryExample example);

    FunctionalCaseHistory selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseHistory record, @Param("example") FunctionalCaseHistoryExample example);

    int updateByExampleWithBLOBs(@Param("record") FunctionalCaseHistory record, @Param("example") FunctionalCaseHistoryExample example);

    int updateByExample(@Param("record") FunctionalCaseHistory record, @Param("example") FunctionalCaseHistoryExample example);

    int updateByPrimaryKeySelective(FunctionalCaseHistory record);

    int updateByPrimaryKeyWithBLOBs(FunctionalCaseHistory record);

    int updateByPrimaryKey(FunctionalCaseHistory record);

    int batchInsert(@Param("list") List<FunctionalCaseHistory> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseHistory> list, @Param("selective") FunctionalCaseHistory.Column ... selective);
}