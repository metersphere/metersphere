package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.CaseReviewExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewMapper {
    long countByExample(CaseReviewExample example);

    int deleteByExample(CaseReviewExample example);

    int deleteByPrimaryKey(String id);

    int insert(CaseReview record);

    int insertSelective(CaseReview record);

    List<CaseReview> selectByExample(CaseReviewExample example);

    CaseReview selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CaseReview record, @Param("example") CaseReviewExample example);

    int updateByExample(@Param("record") CaseReview record, @Param("example") CaseReviewExample example);

    int updateByPrimaryKeySelective(CaseReview record);

    int updateByPrimaryKey(CaseReview record);

    int batchInsert(@Param("list") List<CaseReview> list);

    int batchInsertSelective(@Param("list") List<CaseReview> list, @Param("selective") CaseReview.Column ... selective);
}