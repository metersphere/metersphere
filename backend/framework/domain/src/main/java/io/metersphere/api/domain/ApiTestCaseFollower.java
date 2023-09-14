package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ApiTestCaseFollower implements Serializable {
    @Schema(description = "用例fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_follower.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case_follower.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description = "关注人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_follower.user_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case_follower.user_id.length_range}", groups = {Created.class, Updated.class})
    private String userId;

    private static final long serialVersionUID = 1L;

    public enum Column {
        caseId("case_id", "caseId", "VARCHAR", false),
        userId("user_id", "userId", "VARCHAR", false);

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