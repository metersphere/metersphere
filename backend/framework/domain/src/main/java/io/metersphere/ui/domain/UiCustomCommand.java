package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiCustomCommand implements Serializable {
    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(title = "模块路径", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.module_path.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1000, message = "{ui_custom_command.module_path.length_range}", groups = {Created.class, Updated.class})
    private String modulePath;

    @Schema(title = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ui_custom_command.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "用例等级")
    private String level;

    @Schema(title = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{ui_custom_command.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "责任人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.principal.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "最后执行结果")
    private String lastResult;

    @Schema(title = "num")
    private Integer num;

    @Schema(title = "删除状态")
    private Boolean deleted;

    @Schema(title = "自定义num")
    private String customNum;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_custom_command.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(title = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(title = "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_custom_command.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_custom_command.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(title = "是否为最新版本 0:否，1:是")
    private Boolean latest;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        tags("tags", "tags", "VARCHAR", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        modulePath("module_path", "modulePath", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        level("level", "level", "VARCHAR", true),
        status("status", "status", "VARCHAR", true),
        principal("principal", "principal", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        lastResult("last_result", "lastResult", "VARCHAR", false),
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