package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UserView implements Serializable {
    @Schema(description = "视图ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_view.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_view.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    @Schema(description = "视图名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user_view.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "视图类型，例如功能用例视图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.view_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_view.view_type.length_range}", groups = {Created.class, Updated.class})
    private String viewType;

    @Schema(description = "视图的应用范围，一般为项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_view.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user_view.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "匹配模式：AND/OR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_view.search_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{user_view.search_mode.length_range}", groups = {Created.class, Updated.class})
    private String searchMode;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        userId("user_id", "userId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        viewType("view_type", "viewType", "VARCHAR", false),
        scopeId("scope_id", "scopeId", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        searchMode("search_mode", "searchMode", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false);

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