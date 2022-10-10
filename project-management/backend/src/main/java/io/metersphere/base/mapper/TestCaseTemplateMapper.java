package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseTemplate;
import io.metersphere.base.domain.TestCaseTemplateExample;
import io.metersphere.base.domain.TestCaseTemplateWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseTemplateMapper {
    long countByExample(TestCaseTemplateExample example);

    int deleteByExample(TestCaseTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseTemplateWithBLOBs record);

    int insertSelective(TestCaseTemplateWithBLOBs record);

    List<TestCaseTemplateWithBLOBs> selectByExampleWithBLOBs(TestCaseTemplateExample example);

    List<TestCaseTemplate> selectByExample(TestCaseTemplateExample example);

    TestCaseTemplateWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseTemplateWithBLOBs record, @Param("example") TestCaseTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") TestCaseTemplateWithBLOBs record, @Param("example") TestCaseTemplateExample example);

    int updateByExample(@Param("record") TestCaseTemplate record, @Param("example") TestCaseTemplateExample example);

    int updateByPrimaryKeySelective(TestCaseTemplateWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TestCaseTemplateWithBLOBs record);

    int updateByPrimaryKey(TestCaseTemplate record);
}