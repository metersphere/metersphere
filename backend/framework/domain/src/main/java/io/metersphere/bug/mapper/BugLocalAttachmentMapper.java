package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugLocalAttachment;
import io.metersphere.bug.domain.BugLocalAttachmentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BugLocalAttachmentMapper {
    long countByExample(BugLocalAttachmentExample example);

    int deleteByExample(BugLocalAttachmentExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugLocalAttachment record);

    int insertSelective(BugLocalAttachment record);

    List<BugLocalAttachment> selectByExample(BugLocalAttachmentExample example);

    BugLocalAttachment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugLocalAttachment record, @Param("example") BugLocalAttachmentExample example);

    int updateByExample(@Param("record") BugLocalAttachment record, @Param("example") BugLocalAttachmentExample example);

    int updateByPrimaryKeySelective(BugLocalAttachment record);

    int updateByPrimaryKey(BugLocalAttachment record);

    int batchInsert(@Param("list") List<BugLocalAttachment> list);

    int batchInsertSelective(@Param("list") List<BugLocalAttachment> list, @Param("selective") BugLocalAttachment.Column ... selective);
}