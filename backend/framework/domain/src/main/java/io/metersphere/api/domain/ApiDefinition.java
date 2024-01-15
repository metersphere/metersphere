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
public class ApiDefinition implements Serializable {
    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.protocol.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_definition.protocol.length_range}", groups = {Created.class, Updated.class})
    private String protocol;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    @Schema(description = "接口状态/进行中/已完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "自定义id")
    private Long num;

    @Schema(description = "标签")
    private java.util.List<String> tags;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition.latest.not_blank}", groups = {Created.class})
    private Boolean latest;

    @Schema(description = "版本fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description = "版本引用fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        protocol("protocol", "protocol", "VARCHAR", false),
        method("method", "method", "VARCHAR", true),
        path("path", "path", "VARCHAR", true),
        status("status", "status", "VARCHAR", true),
        num("num", "num", "BIGINT", false),
        tags("tags", "tags", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        latest("latest", "latest", "BIT", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        refId("ref_id", "refId", "VARCHAR", false),
        description("description", "description", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleted("deleted", "deleted", "BIT", false);

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