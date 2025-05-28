package io.metersphere.system.mapper;

import io.metersphere.system.domain.AiConversationContent;
import io.metersphere.system.domain.AiConversationContentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AiConversationContentMapper {
    long countByExample(AiConversationContentExample example);

    int deleteByExample(AiConversationContentExample example);

    int insert(AiConversationContent record);

    int insertSelective(AiConversationContent record);

    List<AiConversationContent> selectByExampleWithBLOBs(AiConversationContentExample example);

    List<AiConversationContent> selectByExample(AiConversationContentExample example);

    int updateByExampleSelective(@Param("record") AiConversationContent record, @Param("example") AiConversationContentExample example);

    int updateByExampleWithBLOBs(@Param("record") AiConversationContent record, @Param("example") AiConversationContentExample example);

    int updateByExample(@Param("record") AiConversationContent record, @Param("example") AiConversationContentExample example);

    int batchInsert(@Param("list") List<AiConversationContent> list);

    int batchInsertSelective(@Param("list") List<AiConversationContent> list, @Param("selective") AiConversationContent.Column ... selective);
}