package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.domain.FunctionalCaseBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseBlobMapper {
    long countByExample(FunctionalCaseBlobExample example);

    int deleteByExample(FunctionalCaseBlobExample example);

    int deleteByPrimaryKey(String functionalCaseId);

    int insert(FunctionalCaseBlob record);

    int insertSelective(FunctionalCaseBlob record);

    List<FunctionalCaseBlob> selectByExampleWithBLOBs(FunctionalCaseBlobExample example);

    List<FunctionalCaseBlob> selectByExample(FunctionalCaseBlobExample example);

    FunctionalCaseBlob selectByPrimaryKey(String functionalCaseId);

    int updateByExampleSelective(@Param("record") FunctionalCaseBlob record, @Param("example") FunctionalCaseBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") FunctionalCaseBlob record, @Param("example") FunctionalCaseBlobExample example);

    int updateByExample(@Param("record") FunctionalCaseBlob record, @Param("example") FunctionalCaseBlobExample example);

    int updateByPrimaryKeySelective(FunctionalCaseBlob record);

    int updateByPrimaryKeyWithBLOBs(FunctionalCaseBlob record);
}