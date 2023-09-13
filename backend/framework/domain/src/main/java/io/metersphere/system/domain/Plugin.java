package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class Plugin implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{plugin.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "插件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{plugin.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "插件ID（名称加版本号）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.plugin_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{plugin.plugin_id.length_range}", groups = {Created.class, Updated.class})
    private String pluginId;

    @Schema(description =  "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.file_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{plugin.file_name.length_range}", groups = {Created.class, Updated.class})
    private String fileName;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "是否启用插件")
    private Boolean enable;

    @Schema(description =  "是否是全局插件")
    private Boolean global;

    @Schema(description =  "是否是企业版插件")
    private Boolean xpack;

    @Schema(description =  "插件描述")
    private String description;

    @Schema(description =  "插件使用场景API_PROTOCOL/PLATFORM", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin.scenario.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{plugin.scenario.length_range}", groups = {Created.class, Updated.class})
    private String scenario;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        pluginId("plugin_id", "pluginId", "VARCHAR", false),
        fileName("file_name", "fileName", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        enable("enable", "enable", "BIT", true),
        global("global", "global", "BIT", true),
        xpack("xpack", "xpack", "BIT", false),
        description("description", "description", "VARCHAR", false),
        scenario("scenario", "scenario", "VARCHAR", false);

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