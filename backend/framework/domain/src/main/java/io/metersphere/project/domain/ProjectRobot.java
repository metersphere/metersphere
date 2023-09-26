package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ProjectRobot implements Serializable {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_robot.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_robot.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_robot.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{project_robot.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_robot.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{project_robot.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "所属平台（飞书:LARK，钉钉:DING_TALK，企业微信:WE_COM，自定义:CUSTOM, 站内信:IN_SITE, 邮件:MAIL）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_robot.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{project_robot.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(description = "webhook", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_robot.webhook.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{project_robot.webhook.length_range}", groups = {Created.class, Updated.class})
    private String webhook;

    @Schema(description = "钉钉机器人的种类: 自定义:CUSTOM, 企业内部:ENTERPRISE")
    private String type;

    @Schema(description = "钉钉AppKey")
    private String appKey;

    @Schema(description = "钉钉AppSecret")
    private String appSecret;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "描述")
    private String description;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        platform("platform", "platform", "VARCHAR", false),
        webhook("webhook", "webhook", "VARCHAR", false),
        type("type", "type", "VARCHAR", true),
        appKey("app_key", "appKey", "VARCHAR", false),
        appSecret("app_secret", "appSecret", "VARCHAR", false),
        enable("enable", "enable", "BIT", true),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
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