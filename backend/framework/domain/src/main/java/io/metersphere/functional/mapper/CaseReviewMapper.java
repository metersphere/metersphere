package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.CaseReviewExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewMapper {
    long countByExample(CaseReviewExample example);

    int deleteByExample(CaseReviewExample example);

    int deleteByPrimaryKey(@Param("id") String id, @Param("reviewPassRule") String reviewPassRule);

    int insert(CaseReview record);

    int insertSelective(CaseReview record);

    List<CaseReview> selectByExampleWithBLOBs(CaseReviewExample example);

    List<CaseReview> selectByExample(CaseReviewExample example);

    CaseReview selectByPrimaryKey(@Param("id") String id, @Param("reviewPassRule") String reviewPassRule);

    int updateByExampleSelective(@Param("record") CaseReview record, @Param("example") CaseReviewExample example);

    int updateByExampleWithBLOBs(@Param("record") CaseReview record, @Param("example") CaseReviewExample example);

    int updateByExample(@Param("record") CaseReview record, @Param("example") CaseReviewExample example);

    int updateByPrimaryKeySelective(CaseReview record);

    int updateByPrimaryKeyWithBLOBs(CaseReview record);

    int updateByPrimaryKey(CaseReview record);
}