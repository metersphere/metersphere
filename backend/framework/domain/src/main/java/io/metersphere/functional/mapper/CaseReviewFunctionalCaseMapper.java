package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewFunctionalCase;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewFunctionalCaseMapper {
    long countByExample(CaseReviewFunctionalCaseExample example);

    int deleteByExample(CaseReviewFunctionalCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(CaseReviewFunctionalCase record);

    int insertSelective(CaseReviewFunctionalCase record);

    List<CaseReviewFunctionalCase> selectByExample(CaseReviewFunctionalCaseExample example);

    CaseReviewFunctionalCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CaseReviewFunctionalCase record, @Param("example") CaseReviewFunctionalCaseExample example);

    int updateByExample(@Param("record") CaseReviewFunctionalCase record, @Param("example") CaseReviewFunctionalCaseExample example);

    int updateByPrimaryKeySelective(CaseReviewFunctionalCase record);

    int updateByPrimaryKey(CaseReviewFunctionalCase record);

    int batchInsert(@Param("list") List<CaseReviewFunctionalCase> list);

    int batchInsertSelective(@Param("list") List<CaseReviewFunctionalCase> list, @Param("selective") CaseReviewFunctionalCase.Column ... selective);
}