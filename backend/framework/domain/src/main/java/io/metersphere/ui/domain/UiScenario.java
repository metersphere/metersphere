package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiScenario implements Serializable {
    @Schema(description =  "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "标签")
    private String tags;

    @Schema(description =  "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description =  "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ui_scenario.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.level.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{ui_scenario.level.length_range}", groups = {Created.class, Updated.class})
    private String level;

    @Schema(description =  "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{ui_scenario.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "责任人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.principal.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(description =  "步骤总数")
    private Integer stepTotal;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "最后执行结果")
    private String lastResult;

    @Schema(description =  "最后执行结果的报告ID")
    private String reportId;

    @Schema(description =  "num")
    private Integer num;

    @Schema(description =  "删除状态")
    private Boolean deleted;

    @Schema(description =  "自定义num")
    private String customNum;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "删除时间")
    private Long deleteTime;

    @Schema(description =  "删除人")
    private String deleteUser;

    @Schema(description =  "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description =  "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description =  "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(description =  "是否为最新版本 0:否，1:是")
    private Boolean latest;

    @Schema(description =  "描述")
    private String description;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        tags("tags", "tags", "VARCHAR", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        level("level", "level", "VARCHAR", true),
        status("status", "status", "VARCHAR", true),
        principal("principal", "principal", "VARCHAR", false),
        stepTotal("step_total", "stepTotal", "INTEGER", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        lastResult("last_result", "lastResult", "VARCHAR", false),
        reportId("report_id", "reportId", "VARCHAR", false),
        num("num", "num", "INTEGER", false),
        deleted("deleted", "deleted", "BIT", false),
        customNum("custom_num", "customNum", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        refId("ref_id", "refId", "VARCHAR", false),
        latest("latest", "latest", "BIT", false),
        description("description", "description", "VARCHAR", false);

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