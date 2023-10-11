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
public class FunctionalCaseRelationshipEdge implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "源节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.source_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.source_id.length_range}", groups = {Created.class, Updated.class})
    private String sourceId;

    @Schema(description =  "目标节点的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.target_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.target_id.length_range}", groups = {Created.class, Updated.class})
    private String targetId;

    @Schema(description =  "所属关系图的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_relationship_edge.graph_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.graph_id.length_range}", groups = {Created.class, Updated.class})
    private String graphId;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "创建时间")
    private Long createTime;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        sourceId("source_id", "sourceId", "VARCHAR", false),
        targetId("target_id", "targetId", "VARCHAR", false),
        graphId("graph_id", "graphId", "VARCHAR", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
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