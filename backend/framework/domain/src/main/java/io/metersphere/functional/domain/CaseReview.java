package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class CaseReview implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{case_review.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 200, message = "{case_review.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "评审状态：未开始/进行中/已完成/已结束/已归档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{case_review.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "评审结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{case_review.end_time.not_blank}", groups = {Created.class})
    private Long endTime;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "评审规则：单人通过/全部通过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{case_review.review_pass_rule.length_range}", groups = {Created.class, Updated.class})
    private String reviewPassRule;

    @Schema(description = "描述")
    private String description;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        status("status", "status", "VARCHAR", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        endTime("end_time", "endTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        tags("tags", "tags", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        reviewPassRule("review_pass_rule", "reviewPassRule", "VARCHAR", false),
        description("description", "description", "LONGVARCHAR", false);

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