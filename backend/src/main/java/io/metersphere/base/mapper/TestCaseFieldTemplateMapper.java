package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseFieldTemplate;
import io.metersphere.base.domain.TestCaseFieldTemplateExample;
import io.metersphere.base.domain.TestCaseFieldTemplateWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseFieldTemplateMapper {
    long countByExample(TestCaseFieldTemplateExample example);

    int deleteByExample(TestCaseFieldTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseFieldTemplateWithBLOBs record);

    int insertSelective(TestCaseFieldTemplateWithBLOBs record);

    List<TestCaseFieldTemplateWithBLOBs> selectByExampleWithBLOBs(TestCaseFieldTemplateExample example);

    List<TestCaseFieldTemplate> selectByExample(TestCaseFieldTemplateExample example);

    TestCaseFieldTemplateWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseFieldTemplateWithBLOBs record, @Param("example") TestCaseFieldTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") TestCaseFieldTemplateWithBLOBs record, @Param("example") TestCaseFieldTemplateExample example);

    int updateByExample(@Param("record") TestCaseFieldTemplate record, @Param("example") TestCaseFieldTemplateExample example);

    int updateByPrimaryKeySelective(TestCaseFieldTemplateWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TestCaseFieldTemplateWithBLOBs record);

    int updateByPrimaryKey(TestCaseFieldTemplate record);
}