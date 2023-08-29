package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class Template implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{template.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{template.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "备注")
    private String remark;

    @Schema(title = "是否是内置模板")
    private Boolean internal;

    @Schema(title = "创建时间")
    private Long updateTime;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "组织或项目级别字段（PROJECT, ORGANIZATION）")
    private String scopeType;

    @Schema(title = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

    @Schema(title = "是否开启api字段名配置")
    private Boolean enableThirdPart;

    @Schema(title = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scene.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{template.scene.length_range}", groups = {Created.class, Updated.class})
    private String scene;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        remark("remark", "remark", "VARCHAR", false),
        internal("internal", "internal", "BIT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        scopeType("scope_type", "scopeType", "VARCHAR", false),
        scopeId("scope_id", "scopeId", "VARCHAR", false),
        enableThirdPart("enable_third_part", "enableThirdPart", "BIT", false),
        scene("scene", "scene", "VARCHAR", false);

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