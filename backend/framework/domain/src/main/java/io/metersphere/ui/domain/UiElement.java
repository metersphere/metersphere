package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiElement implements Serializable {
    @Schema(description =  "元素id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "元素num")
    private Integer num;

    @Schema(description =  "元素所属模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description =  "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ui_element.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "定位类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.location_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{ui_element.location_type.length_range}", groups = {Created.class, Updated.class})
    private String locationType;

    @Schema(description =  "元素定位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.location.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{ui_element.location.length_range}", groups = {Created.class, Updated.class})
    private String location;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "更新人")
    private String updateUser;

    @Schema(description =  "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description =  "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(description =  "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_element.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description =  "是否为最新版本 0:否，1:是")
    private Boolean latest;

    @Schema(description =  "元素描述")
    private String description;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        num("num", "num", "INTEGER", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        locationType("location_type", "locationType", "VARCHAR", false),
        location("location", "location", "VARCHAR", true),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        refId("ref_id", "refId", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        latest("latest", "latest", "BIT", false),
        description("description", "description", "VARCHAR", false),
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