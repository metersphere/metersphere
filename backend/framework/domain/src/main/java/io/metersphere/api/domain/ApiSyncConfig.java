package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiSyncConfig implements Serializable {
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_sync_config.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "API/CASE 来源fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_sync_config.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description = "来源类型/API/CASE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_sync_config.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_sync_config.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(description = "是否隐藏")
    private Boolean hide;

    @Schema(description = "是否通知用例创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_sync_config.notify_case_creator.not_blank}", groups = {Created.class})
    private Boolean notifyCaseCreator;

    @Schema(description = "是否通知场景创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_sync_config.notify_scenario_creator.not_blank}", groups = {Created.class})
    private Boolean notifyScenarioCreator;

    @Schema(description = "是否同步用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_sync_config.sync_enable.not_blank}", groups = {Created.class})
    private Boolean syncEnable;

    @Schema(description = "是否发送通知")
    private Boolean noticeEnable;

    @Schema(description = "同步规则")
    private String ruleConfig;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        resourceType("resource_type", "resourceType", "VARCHAR", false),
        hide("hide", "hide", "BIT", false),
        notifyCaseCreator("notify_case_creator", "notifyCaseCreator", "BIT", false),
        notifyScenarioCreator("notify_scenario_creator", "notifyScenarioCreator", "BIT", false),
        syncEnable("sync_enable", "syncEnable", "BIT", false),
        noticeEnable("notice_enable", "noticeEnable", "BIT", false),
        ruleConfig("rule_config", "ruleConfig", "LONGVARCHAR", false);

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