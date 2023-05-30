package io.metersphere.issue.mapper;

import io.metersphere.issue.domain.IssueAttachment;
import io.metersphere.issue.domain.IssueAttachmentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueAttachmentMapper {
    long countByExample(IssueAttachmentExample example);

    int deleteByExample(IssueAttachmentExample example);

    int deleteByPrimaryKey(@Param("issueId") String issueId, @Param("fileId") String fileId);

    int insert(IssueAttachment record);

    int insertSelective(IssueAttachment record);

    List<IssueAttachment> selectByExample(IssueAttachmentExample example);

    int updateByExampleSelective(@Param("record") IssueAttachment record, @Param("example") IssueAttachmentExample example);

    int updateByExample(@Param("record") IssueAttachment record, @Param("example") IssueAttachmentExample example);
}