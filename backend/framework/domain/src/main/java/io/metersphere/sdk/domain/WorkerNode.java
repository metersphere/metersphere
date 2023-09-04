package io.metersphere.sdk.domain;

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
public class WorkerNode implements Serializable {
    @Schema(description = "auto increment id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{worker_node.id.not_blank}", groups = {Updated.class})
    private Long id;

    @Schema(description = "host name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{worker_node.host_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{worker_node.host_name.length_range}", groups = {Created.class, Updated.class})
    private String hostName;

    @Schema(description = "port", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{worker_node.port.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{worker_node.port.length_range}", groups = {Created.class, Updated.class})
    private String port;

    @Schema(description = "node type: ACTUAL or CONTAINER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{worker_node.type.not_blank}", groups = {Created.class})
    private Integer type;

    @Schema(description = "launch date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{worker_node.launch_date.not_blank}", groups = {Created.class})
    private Long launchDate;

    @Schema(description = "modified time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{worker_node.modified.not_blank}", groups = {Created.class})
    private Long modified;

    @Schema(description = "created time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{worker_node.created.not_blank}", groups = {Created.class})
    private Long created;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "BIGINT", false),
        hostName("host_name", "hostName", "VARCHAR", false),
        port("port", "port", "VARCHAR", false),
        type("type", "type", "INTEGER", true),
        launchDate("launch_date", "launchDate", "BIGINT", false),
        modified("modified", "modified", "BIGINT", false),
        created("created", "created", "BIGINT", false);

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