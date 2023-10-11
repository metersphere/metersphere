package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.domain.FunctionalCaseCommentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseCommentMapper {
    long countByExample(FunctionalCaseCommentExample example);

    int deleteByExample(FunctionalCaseCommentExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseComment record);

    int insertSelective(FunctionalCaseComment record);

    List<FunctionalCaseComment> selectByExampleWithBLOBs(FunctionalCaseCommentExample example);

    List<FunctionalCaseComment> selectByExample(FunctionalCaseCommentExample example);

    FunctionalCaseComment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseComment record, @Param("example") FunctionalCaseCommentExample example);

    int updateByExampleWithBLOBs(@Param("record") FunctionalCaseComment record, @Param("example") FunctionalCaseCommentExample example);

    int updateByExample(@Param("record") FunctionalCaseComment record, @Param("example") FunctionalCaseCommentExample example);

    int updateByPrimaryKeySelective(FunctionalCaseComment record);

    int updateByPrimaryKeyWithBLOBs(FunctionalCaseComment record);

    int updateByPrimaryKey(FunctionalCaseComment record);

    int batchInsert(@Param("list") List<FunctionalCaseComment> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseComment> list, @Param("selective") FunctionalCaseComment.Column ... selective);
}