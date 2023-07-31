package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiDefinition implements Serializable {
    @Schema(title = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "修改时间")
    private Long updateTime;

    @Schema(title = "修改人")
    private String updateUser;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除状态")
    private Boolean deleted;

    @Schema(title = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "接口类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.method.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.method.length_range}", groups = {Created.class, Updated.class})
    private String method;

    @Schema(title = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.protocol.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_definition.protocol.length_range}", groups = {Created.class, Updated.class})
    private String protocol;

    @Schema(title = "接口路径-只有HTTP协议有值")
    private String path;

    @Schema(title = "模块全路径-用于导入处理")
    private String modulePath;

    @Schema(title = "接口状态/进行中/已完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "模块fk")
    private String moduleId;

    @Schema(title = "自定义id")
    private Integer num;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(title = "是否启用同步")
    private Boolean syncEnable;

    @Schema(title = "同步开始时间")
    private Long syncTime;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "环境fk")
    private String environmentId;

    @Schema(title = "是否为最新版本 0:否，1:是")
    private Boolean latest;

    @Schema(title = "版本fk")
    private String versionId;

    @Schema(title = "版本引用fk")
    private String refId;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleted("deleted", "deleted", "BIT", false),
        name("name", "name", "VARCHAR", true),
        method("method", "method", "VARCHAR", true),
        protocol("protocol", "protocol", "VARCHAR", false),
        path("path", "path", "VARCHAR", true),
        modulePath("module_path", "modulePath", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        num("num", "num", "INTEGER", false),
        tags("tags", "tags", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        syncEnable("sync_enable", "syncEnable", "BIT", false),
        syncTime("sync_time", "syncTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        environmentId("environment_id", "environmentId", "VARCHAR", false),
        latest("latest", "latest", "BIT", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        refId("ref_id", "refId", "VARCHAR", false),
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