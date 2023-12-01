package io.metersphere.system.mapper;

import io.metersphere.system.domain.OperationHistory;
import io.metersphere.system.domain.OperationHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OperationHistoryMapper {
    long countByExample(OperationHistoryExample example);

    int deleteByExample(OperationHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OperationHistory record);

    int insertSelective(OperationHistory record);

    List<OperationHistory> selectByExample(OperationHistoryExample example);

    OperationHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") OperationHistory record, @Param("example") OperationHistoryExample example);

    int updateByExample(@Param("record") OperationHistory record, @Param("example") OperationHistoryExample example);

    int updateByPrimaryKeySelective(OperationHistory record);

    int updateByPrimaryKey(OperationHistory record);

    int batchInsert(@Param("list") List<OperationHistory> list);

    int batchInsertSelective(@Param("list") List<OperationHistory> list, @Param("selective") OperationHistory.Column ... selective);
}