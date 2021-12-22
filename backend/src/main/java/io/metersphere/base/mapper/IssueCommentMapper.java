package io.metersphere.base.mapper;

import io.metersphere.base.domain.IssueComment;
import io.metersphere.base.domain.IssueCommentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueCommentMapper {
    long countByExample(IssueCommentExample example);

    int deleteByExample(IssueCommentExample example);

    int deleteByPrimaryKey(String id);

    int insert(IssueComment record);

    int insertSelective(IssueComment record);

    List<IssueComment> selectByExampleWithBLOBs(IssueCommentExample example);

    List<IssueComment> selectByExample(IssueCommentExample example);

    IssueComment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") IssueComment record, @Param("example") IssueCommentExample example);

    int updateByExampleWithBLOBs(@Param("record") IssueComment record, @Param("example") IssueCommentExample example);

    int updateByExample(@Param("record") IssueComment record, @Param("example") IssueCommentExample example);

    int updateByPrimaryKeySelective(IssueComment record);

    int updateByPrimaryKeyWithBLOBs(IssueComment record);

    int updateByPrimaryKey(IssueComment record);
}