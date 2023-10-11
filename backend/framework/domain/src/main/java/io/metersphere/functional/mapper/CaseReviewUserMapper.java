package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewUser;
import io.metersphere.functional.domain.CaseReviewUserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewUserMapper {
    long countByExample(CaseReviewUserExample example);

    int deleteByExample(CaseReviewUserExample example);

    int deleteByPrimaryKey(@Param("reviewId") String reviewId, @Param("userId") String userId);

    int insert(CaseReviewUser record);

    int insertSelective(CaseReviewUser record);

    List<CaseReviewUser> selectByExample(CaseReviewUserExample example);

    int updateByExampleSelective(@Param("record") CaseReviewUser record, @Param("example") CaseReviewUserExample example);

    int updateByExample(@Param("record") CaseReviewUser record, @Param("example") CaseReviewUserExample example);

    int batchInsert(@Param("list") List<CaseReviewUser> list);

    int batchInsertSelective(@Param("list") List<CaseReviewUser> list, @Param("selective") CaseReviewUser.Column ... selective);
}