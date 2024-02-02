package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class CaseReviewHistory implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_history.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{case_review_history.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "评审ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_history.review_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_history.review_id.length_range}", groups = {Created.class, Updated.class})
    private String reviewId;

    @Schema(description = "用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_history.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_history.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description = "评审结果：通过/不通过/建议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_history.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{case_review_history.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "是否是取消关联或评审被删除的：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{case_review_history.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "通知人")
    private String notifier;

    @Schema(description = "操作人")
    private String createUser;

    @Schema(description = "操作时间")
    private Long createTime;

    @Schema(description = "是否是废弃的评审记录：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{case_review_history.abandoned.not_blank}", groups = {Created.class})
    private Boolean abandoned;

    @Schema(description = "评审意见")
    private byte[] content;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        reviewId("review_id", "reviewId", "VARCHAR", false),
        caseId("case_id", "caseId", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        deleted("deleted", "deleted", "BIT", false),
        notifier("notifier", "notifier", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        abandoned("abandoned", "abandoned", "BIT", false),
        content("content", "content", "LONGVARBINARY", false);

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