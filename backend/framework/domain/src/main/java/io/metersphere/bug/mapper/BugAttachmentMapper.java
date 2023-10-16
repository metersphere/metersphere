package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugAttachment;
import io.metersphere.bug.domain.BugAttachmentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugAttachmentMapper {
    long countByExample(BugAttachmentExample example);

    int deleteByExample(BugAttachmentExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugAttachment record);

    int insertSelective(BugAttachment record);

    List<BugAttachment> selectByExample(BugAttachmentExample example);

    BugAttachment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugAttachment record, @Param("example") BugAttachmentExample example);

    int updateByExample(@Param("record") BugAttachment record, @Param("example") BugAttachmentExample example);

    int updateByPrimaryKeySelective(BugAttachment record);

    int updateByPrimaryKey(BugAttachment record);

    int batchInsert(@Param("list") List<BugAttachment> list);

    int batchInsertSelective(@Param("list") List<BugAttachment> list, @Param("selective") BugAttachment.Column ... selective);
}