package io.metersphere.system.mapper;

import io.metersphere.system.domain.OperationLogHistory;
import io.metersphere.system.domain.OperationLogHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OperationLogHistoryMapper {
    long countByExample(OperationLogHistoryExample example);

    int deleteByExample(OperationLogHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OperationLogHistory record);

    int insertSelective(OperationLogHistory record);

    List<OperationLogHistory> selectByExample(OperationLogHistoryExample example);

    OperationLogHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") OperationLogHistory record, @Param("example") OperationLogHistoryExample example);

    int updateByExample(@Param("record") OperationLogHistory record, @Param("example") OperationLogHistoryExample example);

    int updateByPrimaryKeySelective(OperationLogHistory record);

    int updateByPrimaryKey(OperationLogHistory record);

    int batchInsert(@Param("list") List<OperationLogHistory> list);

    int batchInsertSelective(@Param("list") List<OperationLogHistory> list, @Param("selective") OperationLogHistory.Column ... selective);
}