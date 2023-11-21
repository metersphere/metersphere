package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewModule;
import io.metersphere.functional.domain.CaseReviewModuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewModuleMapper {
    long countByExample(CaseReviewModuleExample example);

    int deleteByExample(CaseReviewModuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(CaseReviewModule record);

    int insertSelective(CaseReviewModule record);

    List<CaseReviewModule> selectByExample(CaseReviewModuleExample example);

    CaseReviewModule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") CaseReviewModule record, @Param("example") CaseReviewModuleExample example);

    int updateByExample(@Param("record") CaseReviewModule record, @Param("example") CaseReviewModuleExample example);

    int updateByPrimaryKeySelective(CaseReviewModule record);

    int updateByPrimaryKey(CaseReviewModule record);

    int batchInsert(@Param("list") List<CaseReviewModule> list);

    int batchInsertSelective(@Param("list") List<CaseReviewModule> list, @Param("selective") CaseReviewModule.Column ... selective);
}