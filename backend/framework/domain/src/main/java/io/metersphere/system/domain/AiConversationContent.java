package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class AiConversationContent implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_conversation_content.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{ai_conversation_content.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "对话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_conversation_content.conversation_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ai_conversation_content.conversation_id.length_range}", groups = {Created.class, Updated.class})
    private String conversationId;

    @Schema(description = "记录类型（USER, ASSISTANT, SYSTEM, TOOL）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_conversation_content.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{ai_conversation_content.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "对话标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_conversation_content.content.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 65535, message = "{ai_conversation_content.content.length_range}", groups = {Created.class, Updated.class})
    private String content;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        conversationId("conversation_id", "conversationId", "VARCHAR", false),
        type("type", "type", "VARCHAR", true),
        createTime("create_time", "createTime", "BIGINT", false),
        content("content", "content", "LONGVARCHAR", false);

        private static final String BEGINNING_DELIMITER = "`";

        private static final String ENDING_DELIMITER = "`";

        private final String column;

        private final boolean isColumnNameDelimited;

        private final String javaProperty;

        private final String jdbcType;

        public String value() {
            return this.column;
        }

        public String getValue() {
            return this.column;
        }

        public String getJavaProperty() {
            return this.javaProperty;
        }

        public String getJdbcType() {
            return this.jdbcType;
        }

        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        public static Column[] all() {
            return Column.values();
        }

        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}