package io.metersphere.bug.domain;

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
public class Bug implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{bug.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "业务ID")
    private Integer num;

    @Schema(description = "缺陷标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.title.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{bug.title.length_range}", groups = {Created.class, Updated.class})
    private String title;

    @Schema(description = "处理人集合;预留字段, 后续工作台统计可能需要")
    private String handleUsers;

    @Schema(description = "处理人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.handle_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.handle_user.length_range}", groups = {Created.class, Updated.class})
    private String handleUser;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.template_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(description = "缺陷平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "标签")
    private java.util.List<String> tags;

    @Schema(description = "第三方平台缺陷ID")
    private String platformBugId;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{bug.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{bug.pos.not_blank}", groups = {Created.class})
    private Long pos;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        num("num", "num", "INTEGER", false),
        title("title", "title", "VARCHAR", false),
        handleUsers("handle_users", "handleUsers", "VARCHAR", false),
        handleUser("handle_user", "handleUser", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        templateId("template_id", "templateId", "VARCHAR", false),
        platform("platform", "platform", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        tags("tags", "tags", "VARCHAR", false),
        platformBugId("platform_bug_id", "platformBugId", "VARCHAR", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false),
        deleted("deleted", "deleted", "BIT", false),
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