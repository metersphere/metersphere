package io.metersphere.system.domain;

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
public class Schedule implements Serializable {
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{schedule.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "qrtz UUID")
    private String key;

    @Schema(description = "执行类型 cron", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{schedule.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "cron 表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{schedule.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(description = "Schedule Job Class Name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.job.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{schedule.job.length_range}", groups = {Created.class, Updated.class})
    private String job;

    @Schema(description = "资源类型 API/TESL_PLAN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{schedule.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(description = "是否开启")
    private Boolean enable;

    @Schema(description = "资源ID，api_scenario ui_scenario load_test")
    private String resourceId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "配置")
    private String config;

    @Schema(description = "业务ID")
    private Long num;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        key("key", "key", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        value("value", "value", "VARCHAR", true),
        job("job", "job", "VARCHAR", false),
        resourceType("resource_type", "resourceType", "VARCHAR", false),
        enable("enable", "enable", "BIT", true),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        config("config", "config", "VARCHAR", false),
        num("num", "num", "BIGINT", false);

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