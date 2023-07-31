package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FunctionalCaseTemplateExtend implements Serializable {
    @Schema(title = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template_extend.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_template_extend.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用例名称模板")
    private String caseName;

    @Schema(title = "编辑模式模板：步骤模式/文本模式")
    private String stepModel;

    @Schema(title = "前置条件模板")
    private String prerequisite;

    @Schema(title = "步骤描述模板")
    private String stepDescription;

    @Schema(title = "预期结果模板")
    private String expectedResult;

    @Schema(title = "实际结果模板")
    private String actualResult;

    @Schema(title = "用例步骤")
    private String steps;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        caseName("case_name", "caseName", "VARCHAR", false),
        stepModel("step_model", "stepModel", "VARCHAR", false),
        prerequisite("prerequisite", "prerequisite", "LONGVARCHAR", false),
        stepDescription("step_description", "stepDescription", "LONGVARCHAR", false),
        expectedResult("expected_result", "expectedResult", "LONGVARCHAR", false),
        actualResult("actual_result", "actualResult", "LONGVARCHAR", false),
        steps("steps", "steps", "LONGVARCHAR", false);

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