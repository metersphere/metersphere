package io.metersphere.base.mapper;

import io.metersphere.base.domain.IssueTemplate;
import io.metersphere.base.domain.IssueTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueTemplateMapper {
    long countByExample(IssueTemplateExample example);

    int deleteByExample(IssueTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(IssueTemplate record);

    int insertSelective(IssueTemplate record);

    List<IssueTemplate> selectByExampleWithBLOBs(IssueTemplateExample example);

    List<IssueTemplate> selectByExample(IssueTemplateExample example);

    IssueTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") IssueTemplate record, @Param("example") IssueTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") IssueTemplate record, @Param("example") IssueTemplateExample example);

    int updateByExample(@Param("record") IssueTemplate record, @Param("example") IssueTemplateExample example);

    int updateByPrimaryKeySelective(IssueTemplate record);

    int updateByPrimaryKeyWithBLOBs(IssueTemplate record);

    int updateByPrimaryKey(IssueTemplate record);
}