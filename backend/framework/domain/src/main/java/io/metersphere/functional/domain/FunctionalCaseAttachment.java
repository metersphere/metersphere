package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FunctionalCaseAttachment implements Serializable {
    @Schema(description =  "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description =  "文件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.file_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_attachment.file_id.length_range}", groups = {Created.class, Updated.class})
    private String fileId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        caseId("case_id", "caseId", "VARCHAR", false),
        fileId("file_id", "fileId", "VARCHAR", false);

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