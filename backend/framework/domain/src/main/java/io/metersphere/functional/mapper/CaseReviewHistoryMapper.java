package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.CaseReviewHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewHistoryMapper {
    long countByExample(CaseReviewHistoryExample example);

    int deleteByExample(CaseReviewHistoryExample example);

    int deleteByPrimaryKey(String id);

    int insert(CaseReviewHistory record);

    int insertSelective(CaseReviewHistory record);

    List<CaseReviewHistory> selectByExampleWithBLOBs(CaseReviewHistoryExample example);

    List<CaseReviewHistory> selectByExample(CaseReviewHistoryExample example);

    CaseReviewHistory selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CaseReviewHistory record, @Param("example") CaseReviewHistoryExample example);

    int updateByExampleWithBLOBs(@Param("record") CaseReviewHistory record, @Param("example") CaseReviewHistoryExample example);

    int updateByExample(@Param("record") CaseReviewHistory record, @Param("example") CaseReviewHistoryExample example);

    int updateByPrimaryKeySelective(CaseReviewHistory record);

    int updateByPrimaryKeyWithBLOBs(CaseReviewHistory record);

    int updateByPrimaryKey(CaseReviewHistory record);

    int batchInsert(@Param("list") List<CaseReviewHistory> list);

    int batchInsertSelective(@Param("list") List<CaseReviewHistory> list, @Param("selective") CaseReviewHistory.Column ... selective);
}