package io.metersphere.issue.mapper;

import io.metersphere.issue.domain.IssuesFunctionalCase;
import io.metersphere.issue.domain.IssuesFunctionalCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssuesFunctionalCaseMapper {
    long countByExample(IssuesFunctionalCaseExample example);

    int deleteByExample(IssuesFunctionalCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(IssuesFunctionalCase record);

    int insertSelective(IssuesFunctionalCase record);

    List<IssuesFunctionalCase> selectByExample(IssuesFunctionalCaseExample example);

    IssuesFunctionalCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") IssuesFunctionalCase record, @Param("example") IssuesFunctionalCaseExample example);

    int updateByExample(@Param("record") IssuesFunctionalCase record, @Param("example") IssuesFunctionalCaseExample example);

    int updateByPrimaryKeySelective(IssuesFunctionalCase record);

    int updateByPrimaryKey(IssuesFunctionalCase record);
}