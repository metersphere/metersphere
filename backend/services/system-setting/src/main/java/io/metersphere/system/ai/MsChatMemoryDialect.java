package io.metersphere.system.ai;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;

/**
 * @Author: jianxing
 * @CreateTime: 2025-05-27  17:03
 */
public class MsChatMemoryDialect  implements JdbcChatMemoryRepositoryDialect {

    public String getSelectMessagesSql() {
        return "SELECT content, type FROM ai_conversation_content WHERE conversation_id = ? ORDER BY `timestamp`";
    }

    public String getInsertMessageSql() {
        return "INSERT INTO ai_conversation_content (conversation_id, content, type, `timestamp`) VALUES (?, ?, ?, ?)";
    }

    public String getSelectConversationIdsSql() {
        return "SELECT DISTINCT conversation_id FROM ai_conversation_content";
    }

    public String getDeleteMessagesSql() {
        return "DELETE FROM ai_conversation_content WHERE conversation_id = ?";
    }
}
