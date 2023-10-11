package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewFunctionalCaseUser;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseUserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewFunctionalCaseUserMapper {
    long countByExample(CaseReviewFunctionalCaseUserExample example);

    int deleteByExample(CaseReviewFunctionalCaseUserExample example);

    int insert(CaseReviewFunctionalCaseUser record);

    int insertSelective(CaseReviewFunctionalCaseUser record);

    List<CaseReviewFunctionalCaseUser> selectByExample(CaseReviewFunctionalCaseUserExample example);

    int updateByExampleSelective(@Param("record") CaseReviewFunctionalCaseUser record, @Param("example") CaseReviewFunctionalCaseUserExample example);

    int updateByExample(@Param("record") CaseReviewFunctionalCaseUser record, @Param("example") CaseReviewFunctionalCaseUserExample example);

    int batchInsert(@Param("list") List<CaseReviewFunctionalCaseUser> list);

    int batchInsertSelective(@Param("list") List<CaseReviewFunctionalCaseUser> list, @Param("selective") CaseReviewFunctionalCaseUser.Column ... selective);
}