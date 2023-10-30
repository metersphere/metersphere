package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.domain.FunctionalCaseAttachmentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseAttachmentMapper {
    long countByExample(FunctionalCaseAttachmentExample example);

    int deleteByExample(FunctionalCaseAttachmentExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalCaseAttachment record);

    int insertSelective(FunctionalCaseAttachment record);

    List<FunctionalCaseAttachment> selectByExample(FunctionalCaseAttachmentExample example);

    FunctionalCaseAttachment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalCaseAttachment record, @Param("example") FunctionalCaseAttachmentExample example);

    int updateByExample(@Param("record") FunctionalCaseAttachment record, @Param("example") FunctionalCaseAttachmentExample example);

    int updateByPrimaryKeySelective(FunctionalCaseAttachment record);

    int updateByPrimaryKey(FunctionalCaseAttachment record);

    int batchInsert(@Param("list") List<FunctionalCaseAttachment> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseAttachment> list, @Param("selective") FunctionalCaseAttachment.Column ... selective);
}