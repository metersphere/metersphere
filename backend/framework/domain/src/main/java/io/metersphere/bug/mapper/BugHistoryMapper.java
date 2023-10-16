package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugHistory;
import io.metersphere.bug.domain.BugHistoryExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugHistoryMapper {
    long countByExample(BugHistoryExample example);

    int deleteByExample(BugHistoryExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugHistory record);

    int insertSelective(BugHistory record);

    List<BugHistory> selectByExampleWithBLOBs(BugHistoryExample example);

    List<BugHistory> selectByExample(BugHistoryExample example);

    BugHistory selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugHistory record, @Param("example") BugHistoryExample example);

    int updateByExampleWithBLOBs(@Param("record") BugHistory record, @Param("example") BugHistoryExample example);

    int updateByExample(@Param("record") BugHistory record, @Param("example") BugHistoryExample example);

    int updateByPrimaryKeySelective(BugHistory record);

    int updateByPrimaryKeyWithBLOBs(BugHistory record);

    int updateByPrimaryKey(BugHistory record);

    int batchInsert(@Param("list") List<BugHistory> list);

    int batchInsertSelective(@Param("list") List<BugHistory> list, @Param("selective") BugHistory.Column ... selective);
}