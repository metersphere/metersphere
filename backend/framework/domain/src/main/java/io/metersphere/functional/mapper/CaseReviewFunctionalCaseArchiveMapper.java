package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewFunctionalCaseArchive;
import io.metersphere.functional.domain.CaseReviewFunctionalCaseArchiveExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewFunctionalCaseArchiveMapper {
    long countByExample(CaseReviewFunctionalCaseArchiveExample example);

    int deleteByExample(CaseReviewFunctionalCaseArchiveExample example);

    int insert(CaseReviewFunctionalCaseArchive record);

    int insertSelective(CaseReviewFunctionalCaseArchive record);

    List<CaseReviewFunctionalCaseArchive> selectByExampleWithBLOBs(CaseReviewFunctionalCaseArchiveExample example);

    List<CaseReviewFunctionalCaseArchive> selectByExample(CaseReviewFunctionalCaseArchiveExample example);

    int updateByExampleSelective(@Param("record") CaseReviewFunctionalCaseArchive record, @Param("example") CaseReviewFunctionalCaseArchiveExample example);

    int updateByExampleWithBLOBs(@Param("record") CaseReviewFunctionalCaseArchive record, @Param("example") CaseReviewFunctionalCaseArchiveExample example);

    int updateByExample(@Param("record") CaseReviewFunctionalCaseArchive record, @Param("example") CaseReviewFunctionalCaseArchiveExample example);

    int batchInsert(@Param("list") List<CaseReviewFunctionalCaseArchive> list);

    int batchInsertSelective(@Param("list") List<CaseReviewFunctionalCaseArchive> list, @Param("selective") CaseReviewFunctionalCaseArchive.Column ... selective);
}