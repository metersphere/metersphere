package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseCustomFieldMapper {
    long countByExample(FunctionalCaseCustomFieldExample example);

    int deleteByExample(FunctionalCaseCustomFieldExample example);

    int deleteByPrimaryKey(@Param("caseId") String caseId, @Param("fieldId") String fieldId);

    int insert(FunctionalCaseCustomField record);

    int insertSelective(FunctionalCaseCustomField record);

    List<FunctionalCaseCustomField> selectByExampleWithBLOBs(FunctionalCaseCustomFieldExample example);

    List<FunctionalCaseCustomField> selectByExample(FunctionalCaseCustomFieldExample example);

    FunctionalCaseCustomField selectByPrimaryKey(@Param("caseId") String caseId, @Param("fieldId") String fieldId);

    int updateByExampleSelective(@Param("record") FunctionalCaseCustomField record, @Param("example") FunctionalCaseCustomFieldExample example);

    int updateByExampleWithBLOBs(@Param("record") FunctionalCaseCustomField record, @Param("example") FunctionalCaseCustomFieldExample example);

    int updateByExample(@Param("record") FunctionalCaseCustomField record, @Param("example") FunctionalCaseCustomFieldExample example);

    int updateByPrimaryKeySelective(FunctionalCaseCustomField record);

    int updateByPrimaryKeyWithBLOBs(FunctionalCaseCustomField record);

    int updateByPrimaryKey(FunctionalCaseCustomField record);
}