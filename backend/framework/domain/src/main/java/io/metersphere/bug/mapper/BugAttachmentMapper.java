package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugAttachment;
import io.metersphere.bug.domain.BugAttachmentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BugAttachmentMapper {
    long countByExample(BugAttachmentExample example);

    int deleteByExample(BugAttachmentExample example);

    int deleteByPrimaryKey(@Param("bugId") String bugId, @Param("fileId") String fileId);

    int insert(BugAttachment record);

    int insertSelective(BugAttachment record);

    List<BugAttachment> selectByExample(BugAttachmentExample example);

    int updateByExampleSelective(@Param("record") BugAttachment record, @Param("example") BugAttachmentExample example);

    int updateByExample(@Param("record") BugAttachment record, @Param("example") BugAttachmentExample example);
}