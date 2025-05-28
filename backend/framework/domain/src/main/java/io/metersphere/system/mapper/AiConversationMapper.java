package io.metersphere.system.mapper;

import io.metersphere.system.domain.AiConversation;
import io.metersphere.system.domain.AiConversationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AiConversationMapper {
    long countByExample(AiConversationExample example);

    int deleteByExample(AiConversationExample example);

    int deleteByPrimaryKey(String id);

    int insert(AiConversation record);

    int insertSelective(AiConversation record);

    List<AiConversation> selectByExample(AiConversationExample example);

    AiConversation selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") AiConversation record, @Param("example") AiConversationExample example);

    int updateByExample(@Param("record") AiConversation record, @Param("example") AiConversationExample example);

    int updateByPrimaryKeySelective(AiConversation record);

    int updateByPrimaryKey(AiConversation record);

    int batchInsert(@Param("list") List<AiConversation> list);

    int batchInsertSelective(@Param("list") List<AiConversation> list, @Param("selective") AiConversation.Column ... selective);
}