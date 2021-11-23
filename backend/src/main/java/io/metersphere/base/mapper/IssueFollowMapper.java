package io.metersphere.base.mapper;

import io.metersphere.base.domain.IssueFollow;
import io.metersphere.base.domain.IssueFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueFollowMapper {
    long countByExample(IssueFollowExample example);

    int deleteByExample(IssueFollowExample example);

    int insert(IssueFollow record);

    int insertSelective(IssueFollow record);

    List<IssueFollow> selectByExample(IssueFollowExample example);

    int updateByExampleSelective(@Param("record") IssueFollow record, @Param("example") IssueFollowExample example);

    int updateByExample(@Param("record") IssueFollow record, @Param("example") IssueFollowExample example);
}