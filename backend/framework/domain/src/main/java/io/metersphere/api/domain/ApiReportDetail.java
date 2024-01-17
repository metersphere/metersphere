package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiReportDetail implements Serializable {
    @Schema(description = "报告详情id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_detail.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report_detail.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "接口报告id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_detail.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report_detail.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(description = "各个步骤请求唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_detail.step_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_report_detail.step_id.length_range}", groups = {Created.class, Updated.class})
    private String stepId;

    @Schema(description = "结果状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report_detail.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_report_detail.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "误报编号/误报状态独有")
    private String fakeCode;

    @Schema(description = "请求名称")
    private String requestName;

    @Schema(description = "请求耗时", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report_detail.request_time.not_blank}", groups = {Created.class})
    private Long requestTime;

    @Schema(description = "请求响应码")
    private String code;

    @Schema(description = "响应内容大小", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_report_detail.response_size.not_blank}", groups = {Created.class})
    private Long responseSize;

    @Schema(description = "脚本标识")
    private String scriptIdentifier;

    @Schema(description = "结果内容详情")
    private byte[] content;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        reportId("report_id", "reportId", "VARCHAR", false),
        stepId("step_id", "stepId", "VARCHAR", false),
        status("status", "status", "VARCHAR", true),
        fakeCode("fake_code", "fakeCode", "VARCHAR", false),
        requestName("request_name", "requestName", "VARCHAR", false),
        requestTime("request_time", "requestTime", "BIGINT", false),
        code("code", "code", "VARCHAR", false),
        responseSize("response_size", "responseSize", "BIGINT", false),
        scriptIdentifier("script_identifier", "scriptIdentifier", "VARCHAR", false),
        content("content", "content", "LONGVARBINARY", false);

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