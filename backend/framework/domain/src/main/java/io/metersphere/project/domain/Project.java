package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class Project implements Serializable {
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目编号")
    private Long num;

    @Schema(title = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.organization_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{project.organization_id.length_range}", groups = {Created.class, Updated.class})
    private String organizationId;

    @Schema(title = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{project.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "项目描述")
    private String description;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "修改人")
    private String updateUser;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "是否删除")
    private Boolean deleted;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "是否启用")
    private Boolean enable;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        num("num", "num", "BIGINT", false),
        organizationId("organization_id", "organizationId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        description("description", "description", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleted("deleted", "deleted", "BIT", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        enable("enable", "enable", "BIT", true);

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