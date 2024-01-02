package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FunctionalCaseTest implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description = "其他类型用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(description = "用例类型：接口用例/场景用例/性能用例/UI用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.source_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case_test.source_type.length_range}", groups = {Created.class, Updated.class})
    private String sourceType;

    @Schema(description = "用例所属项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "用例的版本id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新人")
    private String updateUser;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        caseId("case_id", "caseId", "VARCHAR", false),
        sourceId("source_id", "sourceId", "VARCHAR", false),
        sourceType("source_type", "sourceType", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false);

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