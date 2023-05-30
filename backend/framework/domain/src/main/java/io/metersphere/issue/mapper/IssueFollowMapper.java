package io.metersphere.issue.mapper;

import io.metersphere.issue.domain.IssueFollow;
import io.metersphere.issue.domain.IssueFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueFollowMapper {
    long countByExample(IssueFollowExample example);

    int deleteByExample(IssueFollowExample example);

    int deleteByPrimaryKey(@Param("issueId") String issueId, @Param("followId") String followId);

    int insert(IssueFollow record);

    int insertSelective(IssueFollow record);

    List<IssueFollow> selectByExample(IssueFollowExample example);

    int updateByExampleSelective(@Param("record") IssueFollow record, @Param("example") IssueFollowExample example);

    int updateByExample(@Param("record") IssueFollow record, @Param("example") IssueFollowExample example);
}