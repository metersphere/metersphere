package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CustomFieldTestCase;
import io.metersphere.functional.domain.CustomFieldTestCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldTestCaseMapper {
    long countByExample(CustomFieldTestCaseExample example);

    int deleteByExample(CustomFieldTestCaseExample example);

    int deleteByPrimaryKey(@Param("resourceId") String resourceId, @Param("fieldId") String fieldId);

    int insert(CustomFieldTestCase record);

    int insertSelective(CustomFieldTestCase record);

    List<CustomFieldTestCase> selectByExampleWithBLOBs(CustomFieldTestCaseExample example);

    List<CustomFieldTestCase> selectByExample(CustomFieldTestCaseExample example);

    CustomFieldTestCase selectByPrimaryKey(@Param("resourceId") String resourceId, @Param("fieldId") String fieldId);

    int updateByExampleSelective(@Param("record") CustomFieldTestCase record, @Param("example") CustomFieldTestCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomFieldTestCase record, @Param("example") CustomFieldTestCaseExample example);

    int updateByExample(@Param("record") CustomFieldTestCase record, @Param("example") CustomFieldTestCaseExample example);

    int updateByPrimaryKeySelective(CustomFieldTestCase record);

    int updateByPrimaryKeyWithBLOBs(CustomFieldTestCase record);

    int updateByPrimaryKey(CustomFieldTestCase record);
}