package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class Environment implements Serializable {
    @Schema(description = "环境ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{environment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "环境名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{environment.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{environment.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{environment.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "是否是mock环境", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{environment.mock.not_blank}", groups = {Created.class})
    private Boolean mock;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{environment.pos.not_blank}", groups = {Created.class})
    private Long pos;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        projectId("project_id", "projectId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        mock("mock", "mock", "BIT", false),
        description("description", "description", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false);

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