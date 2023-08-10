package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiElementScenarioReference implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "元素ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.element_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.element_id.length_range}", groups = {Created.class, Updated.class})
    private String elementId;

    @Schema(description =  "元素模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.element_module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.element_module_id.length_range}", groups = {Created.class, Updated.class})
    private String elementModuleId;

    @Schema(description =  "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String scenarioId;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_element_scenario_reference.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_element_scenario_reference.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        elementId("element_id", "elementId", "VARCHAR", false),
        elementModuleId("element_module_id", "elementModuleId", "VARCHAR", false),
        scenarioId("scenario_id", "scenarioId", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false);

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