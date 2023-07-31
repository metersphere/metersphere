package io.metersphere.project.mapper;

import io.metersphere.project.domain.FunctionalCaseTemplateExtend;
import io.metersphere.project.domain.FunctionalCaseTemplateExtendExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseTemplateExtendMapper {
    long countByExample(FunctionalCaseTemplateExtendExample example);

    int deleteByExample(FunctionalCaseTemplateExtendExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseTemplateExtend record);

    int insertSelective(FunctionalCaseTemplateExtend record);

    List<FunctionalCaseTemplateExtend> selectByExampleWithBLOBs(FunctionalCaseTemplateExtendExample example);

    List<FunctionalCaseTemplateExtend> selectByExample(FunctionalCaseTemplateExtendExample example);

    FunctionalCaseTemplateExtend selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseTemplateExtend record, @Param("example") FunctionalCaseTemplateExtendExample example);

    int updateByExampleWithBLOBs(@Param("record") FunctionalCaseTemplateExtend record, @Param("example") FunctionalCaseTemplateExtendExample example);

    int updateByExample(@Param("record") FunctionalCaseTemplateExtend record, @Param("example") FunctionalCaseTemplateExtendExample example);

    int updateByPrimaryKeySelective(FunctionalCaseTemplateExtend record);

    int updateByPrimaryKeyWithBLOBs(FunctionalCaseTemplateExtend record);

    int updateByPrimaryKey(FunctionalCaseTemplateExtend record);

    int batchInsert(@Param("list") List<FunctionalCaseTemplateExtend> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseTemplateExtend> list, @Param("selective") FunctionalCaseTemplateExtend.Column ... selective);
}