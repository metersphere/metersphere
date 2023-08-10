package io.metersphere.bug.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class Bug implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{bug.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "业务ID")
    private Integer num;

    @Schema(description =  "缺陷标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.title.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{bug.title.length_range}", groups = {Created.class, Updated.class})
    private String title;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "缺陷平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "缺陷来源，记录创建该缺陷的测试计划的ID")
    private String sourceId;

    @Schema(description =  "第三方平台状态")
    private String platformStatus;

    @Schema(description =  "第三方平台缺陷ID")
    private String platformId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        num("num", "num", "INTEGER", false),
        title("title", "title", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        platform("platform", "platform", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        sourceId("source_id", "sourceId", "VARCHAR", false),
        platformStatus("platform_status", "platformStatus", "VARCHAR", false),
        platformId("platform_id", "platformId", "VARCHAR", false);

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