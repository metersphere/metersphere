package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Data
public class ApiTestCase implements Serializable {
    @Schema(description = "接口用例pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_test_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "接口用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_test_case.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.priority.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}", groups = {Created.class, Updated.class})
    private String priority;

    @Schema(description = "接口用例编号id")
    private Long num;

    @Schema(description = "标签")
    private java.util.List<String> tags;

    @Schema(description = "用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_test_case.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "最新执行结果状态")
    private String lastReportStatus;

    @Schema(description = "最后执行结果报告fk")
    private String lastReportId;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    private String apiDefinitionId;

    @Schema(description = "版本fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description = "环境fk")
    private String environmentId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "接口定义参数变更标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.api_change.not_blank}", groups = {Created.class})
    private Boolean apiChange;

    @Schema(description = "忽略接口定义参数变更", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.ignore_api_change.not_blank}", groups = {Created.class})
    private Boolean ignoreApiChange;

    @Schema(description = "忽略接口与用例参数不一致", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.ignore_api_diff.not_blank}", groups = {Created.class})
    private Boolean ignoreApiDiff;

    @Schema(description = "是否是ai自动生成的用例：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case.ai_create.not_blank}", groups = {Created.class})
    private Boolean aiCreate;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        priority("priority", "priority", "VARCHAR", false),
        num("num", "num", "BIGINT", false),
        tags("tags", "tags", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        lastReportStatus("last_report_status", "lastReportStatus", "VARCHAR", false),
        lastReportId("last_report_id", "lastReportId", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        apiDefinitionId("api_definition_id", "apiDefinitionId", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        environmentId("environment_id", "environmentId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        apiChange("api_change", "apiChange", "BIT", false),
        ignoreApiChange("ignore_api_change", "ignoreApiChange", "BIT", false),
        ignoreApiDiff("ignore_api_diff", "ignoreApiDiff", "BIT", false),
        aiCreate("ai_create", "aiCreate", "BIT", false);

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