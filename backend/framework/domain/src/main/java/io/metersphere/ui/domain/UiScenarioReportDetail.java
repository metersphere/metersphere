package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class UiScenarioReportDetail implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_detail.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "资源id（场景，接口）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_detail.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description =  "报告 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_detail.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "结果状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{ui_scenario_report_detail.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "请求时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_report_detail.request_time.not_blank}", groups = {Created.class})
    private Long requestTime;

    @Schema(description =  "总断言数")
    private Long totalAssertions;

    @Schema(description =  "失败断言数")
    private Long passAssertions;

    @Schema(description =  "执行结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_report_detail.content.not_blank}", groups = {Created.class})
    private byte[] content;

    @Schema(description =  "记录截图断言等结果")
    private byte[] baseInfo;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        resourceId("resource_id", "resourceId", "VARCHAR", false),
        reportId("report_id", "reportId", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        status("status", "status", "VARCHAR", true),
        requestTime("request_time", "requestTime", "BIGINT", false),
        totalAssertions("total_assertions", "totalAssertions", "BIGINT", false),
        passAssertions("pass_assertions", "passAssertions", "BIGINT", false),
        content("content", "content", "LONGVARBINARY", false),
        baseInfo("base_info", "baseInfo", "LONGVARBINARY", false);

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