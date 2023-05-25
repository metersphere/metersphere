package io.metersphere.project.mapper;

import io.metersphere.project.domain.FunctionalCaseTemplate;
import io.metersphere.project.domain.FunctionalCaseTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseTemplateMapper {
    long countByExample(FunctionalCaseTemplateExample example);

    int deleteByExample(FunctionalCaseTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseTemplate record);

    int insertSelective(FunctionalCaseTemplate record);

    List<FunctionalCaseTemplate> selectByExample(FunctionalCaseTemplateExample example);

    FunctionalCaseTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseTemplate record, @Param("example") FunctionalCaseTemplateExample example);

    int updateByExample(@Param("record") FunctionalCaseTemplate record, @Param("example") FunctionalCaseTemplateExample example);

    int updateByPrimaryKeySelective(FunctionalCaseTemplate record);

    int updateByPrimaryKey(FunctionalCaseTemplate record);
}