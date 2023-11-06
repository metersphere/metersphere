package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FileMetadataRepository implements Serializable {
    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata_repository.file_metadata_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata_repository.file_metadata_id.length_range}", groups = {Created.class, Updated.class})
    private String fileMetadataId;

    @Schema(description = "分支", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata_repository.branch.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{file_metadata_repository.branch.length_range}", groups = {Created.class, Updated.class})
    private String branch;

    @Schema(description = "提交ID")
    private String commitId;

    @Schema(description = "提交信息")
    private String commitMessage;

    private static final long serialVersionUID = 1L;

    public enum Column {
        fileMetadataId("file_metadata_id", "fileMetadataId", "VARCHAR", false),
        branch("branch", "branch", "VARCHAR", false),
        commitId("commit_id", "commitId", "VARCHAR", false),
        commitMessage("commit_message", "commitMessage", "LONGVARCHAR", false);

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