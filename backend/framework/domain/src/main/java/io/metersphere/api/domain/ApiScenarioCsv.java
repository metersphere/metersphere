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
public class ApiScenarioCsv implements Serializable {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_csv.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_csv.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "文件id/引用文件id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_csv.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_csv.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(description = "场景id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_csv.scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_csv.scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String scenarioId;

    @Schema(description = "csv变量名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_csv.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_scenario_csv.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "作用域 SCENARIO/STEP", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_csv.scope.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_csv.scope.length_range}", groups = {Created.class, Updated.class})
    private String scope;

    @Schema(description = "启用/禁用")
    private Boolean enable;

    @Schema(description = "是否引用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_csv.association.not_blank}", groups = {Created.class})
    private Boolean association;

    @Schema(description = "文件编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_csv.encoding.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_csv.encoding.length_range}", groups = {Created.class, Updated.class})
    private String encoding;

    @Schema(description = "是否随机", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_csv.random.not_blank}", groups = {Created.class})
    private Boolean random;

    @Schema(description = "变量名称(西文逗号间隔)")
    private String variableNames;

    @Schema(description = "忽略首行(只有在设置了变量名称后才生效)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_csv.ignore_first_line.not_blank}", groups = {Created.class})
    private Boolean ignoreFirstLine;

    @Schema(description = "分隔符")
    private String delimiter;

    @Schema(description = "是否允许带引号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_csv.allow_quoted_data.not_blank}", groups = {Created.class})
    private Boolean allowQuotedData;

    @Schema(description = "遇到文件结束符再次循环", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_csv.recycle_on_eof.not_blank}", groups = {Created.class})
    private Boolean recycleOnEof;

    @Schema(description = "遇到文件结束符停止线程", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_scenario_csv.stop_thread_on_eof.not_blank}", groups = {Created.class})
    private Boolean stopThreadOnEof;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_csv.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_csv.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        fileId("file_id", "fileId", "VARCHAR", false),
        scenarioId("scenario_id", "scenarioId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        fileName("file_name", "fileName", "VARCHAR", false),
        scope("scope", "scope", "VARCHAR", true),
        enable("enable", "enable", "BIT", true),
        association("association", "association", "BIT", false),
        encoding("encoding", "encoding", "VARCHAR", true),
        random("random", "random", "BIT", false),
        variableNames("variable_names", "variableNames", "VARCHAR", false),
        ignoreFirstLine("ignore_first_line", "ignoreFirstLine", "BIT", false),
        delimiter("delimiter", "delimiter", "VARCHAR", true),
        allowQuotedData("allow_quoted_data", "allowQuotedData", "BIT", false),
        recycleOnEof("recycle_on_eof", "recycleOnEof", "BIT", false),
        stopThreadOnEof("stop_thread_on_eof", "stopThreadOnEof", "BIT", false),
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

        public static Column[] excludes(Column... excludes) {
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