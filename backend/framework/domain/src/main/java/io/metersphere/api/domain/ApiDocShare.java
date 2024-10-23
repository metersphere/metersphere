package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

@Data
public class ApiDocShare implements Serializable {
    @Schema(title = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_doc_share.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_doc_share.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_doc_share.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_doc_share.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "是否私有;0: 公开、1: 私有", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_doc_share.is_private.not_blank}", groups = {Created.class})
    private Boolean isPrivate;

    @Schema(title = "访问密码; 私有时需要访问密码")
    private String password;

    @Schema(title = "允许导出; 0: 不允许、1: 允许", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_doc_share.allow_export.not_blank}", groups = {Created.class})
    private Boolean allowExport;

    @Schema(title = "接口范围; 全部接口(ALL)、模块(MODULE)、路径(PATH)、标签(TAG)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_doc_share.api_range.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{api_doc_share.api_range.length_range}", groups = {Created.class, Updated.class})
    private String apiRange;

    @Schema(title = "范围匹配符; 包含(CONTAINS)、等于(EQUALS)")
    private String rangeMatchSymbol;

    @Schema(title = "范围匹配值; eg: 选中路径范围时, 该值作为路径匹配")
    private String rangeMatchVal;

    @Schema(title = "截止时间")
    private Long invalidTime;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_doc_share.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_doc_share.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "更新人")
    private String updateUser;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        isPrivate("is_private", "isPrivate", "BIT", false),
        password("password", "password", "VARCHAR", true),
        allowExport("allow_export", "allowExport", "BIT", false),
        apiRange("api_range", "apiRange", "VARCHAR", false),
        rangeMatchSymbol("range_match_symbol", "rangeMatchSymbol", "VARCHAR", false),
        rangeMatchVal("range_match_val", "rangeMatchVal", "VARCHAR", false),
        invalidTime("invalid_time", "invalidTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false);

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