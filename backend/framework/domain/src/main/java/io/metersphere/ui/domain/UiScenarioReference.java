package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiScenarioReference implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_reference.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.ui_scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_reference.ui_scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String uiScenarioId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "被引用的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.reference_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_reference.reference_id.length_range}", groups = {Created.class, Updated.class})
    private String referenceId;

    @Schema(title = "引用的数据类型（场景，指令）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_reference.data_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{ui_scenario_reference.data_type.length_range}", groups = {Created.class, Updated.class})
    private String dataType;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        uiScenarioId("ui_scenario_id", "uiScenarioId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        referenceId("reference_id", "referenceId", "VARCHAR", false),
        dataType("data_type", "dataType", "VARCHAR", false);

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