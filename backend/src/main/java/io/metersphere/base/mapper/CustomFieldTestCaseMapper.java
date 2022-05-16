package io.metersphere.base.mapper;

import io.metersphere.base.domain.CustomFieldTestCase;
import io.metersphere.base.domain.CustomFieldTestCaseExample;
import io.metersphere.base.domain.CustomFieldTestCaseKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomFieldTestCaseMapper {
    long countByExample(CustomFieldTestCaseExample example);

    int deleteByExample(CustomFieldTestCaseExample example);

    int deleteByPrimaryKey(CustomFieldTestCaseKey key);

    int insert(CustomFieldTestCase record);

    int insertSelective(CustomFieldTestCase record);

    List<CustomFieldTestCase> selectByExampleWithBLOBs(CustomFieldTestCaseExample example);

    List<CustomFieldTestCase> selectByExample(CustomFieldTestCaseExample example);

    CustomFieldTestCase selectByPrimaryKey(CustomFieldTestCaseKey key);

    int updateByExampleSelective(@Param("record") CustomFieldTestCase record, @Param("example") CustomFieldTestCaseExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomFieldTestCase record, @Param("example") CustomFieldTestCaseExample example);

    int updateByExample(@Param("record") CustomFieldTestCase record, @Param("example") CustomFieldTestCaseExample example);

    int updateByPrimaryKeySelective(CustomFieldTestCase record);

    int updateByPrimaryKeyWithBLOBs(CustomFieldTestCase record);

    int updateByPrimaryKey(CustomFieldTestCase record);
}