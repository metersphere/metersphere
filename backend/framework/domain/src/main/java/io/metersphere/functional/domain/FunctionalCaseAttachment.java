package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FunctionalCaseAttachment implements Serializable {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description = "文件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.file_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{functional_case_attachment.file_name.length_range}", groups = {Created.class, Updated.class})
    private String fileName;

    @Schema(description = "文件来源", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.file_source.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.file_source.length_range}", groups = {Created.class, Updated.class})
    private String fileSource;

    @Schema(description = "文件大小", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case_attachment.size.not_blank}", groups = {Created.class})
    private Long size;

    @Schema(description = "是否本地", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case_attachment.local.not_blank}", groups = {Created.class})
    private Boolean local;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        caseId("case_id", "caseId", "VARCHAR", false),
        fileId("file_id", "fileId", "VARCHAR", false),
        fileName("file_name", "fileName", "VARCHAR", false),
        fileSource("file_source", "fileSource", "VARCHAR", false),
        size("size", "size", "BIGINT", true),
        local("local", "local", "BIT", true),
        createUser("create_user", "createUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false);

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