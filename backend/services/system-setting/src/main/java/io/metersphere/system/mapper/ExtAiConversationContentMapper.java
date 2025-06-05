package io.metersphere.system.mapper;

import io.metersphere.system.domain.AiConversationContent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtAiConversationContentMapper {

    List<AiConversationContent> selectLastByConversationIdByLimit(@Param("conversationId") String conversationId,
                                                              @Param("limit")int limit);

}