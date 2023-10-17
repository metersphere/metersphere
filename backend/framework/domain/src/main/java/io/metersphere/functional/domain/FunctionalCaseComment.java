package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FunctionalCaseComment implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_comment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_comment.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description =  "评论人")
    private String createUser;

    @Schema(description =  "评审/测试计划执行状态:通过/不通过/重新提审/通过标准变更标记/强制通过标记/强制不通过标记/状态变更标记")
    private String status;

    @Schema(description =  "评论类型：用例评论/测试计划用例评论/评审用例评论", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case_comment.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description =  "父评论ID")
    private String parentId;

    @Schema(description =  "资源ID: 评审ID/测试计划ID")
    private String resourceId;

    @Schema(description =  "通知人")
    private String notifier;

    @Schema(description =  "回复人")
    private String replyUser;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.content.not_blank}", groups = {Created.class})
    private String content;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        caseId("case_id", "caseId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        parentId("parent_id", "parentId", "VARCHAR", false),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        notifier("notifier", "notifier", "VARCHAR", false),
        replyUser("reply_user", "replyUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
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