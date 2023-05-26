package io.metersphere.project.mapper;

import io.metersphere.project.domain.IssueTemplateExtend;
import io.metersphere.project.domain.IssueTemplateExtendExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueTemplateExtendMapper {
    long countByExample(IssueTemplateExtendExample example);

    int deleteByExample(IssueTemplateExtendExample example);

    int deleteByPrimaryKey(String id);

    int insert(IssueTemplateExtend record);

    int insertSelective(IssueTemplateExtend record);

    List<IssueTemplateExtend> selectByExampleWithBLOBs(IssueTemplateExtendExample example);

    List<IssueTemplateExtend> selectByExample(IssueTemplateExtendExample example);

    IssueTemplateExtend selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") IssueTemplateExtend record, @Param("example") IssueTemplateExtendExample example);

    int updateByExampleWithBLOBs(@Param("record") IssueTemplateExtend record, @Param("example") IssueTemplateExtendExample example);

    int updateByExample(@Param("record") IssueTemplateExtend record, @Param("example") IssueTemplateExtendExample example);

    int updateByPrimaryKeySelective(IssueTemplateExtend record);

    int updateByPrimaryKeyWithBLOBs(IssueTemplateExtend record);

    int updateByPrimaryKey(IssueTemplateExtend record);
}