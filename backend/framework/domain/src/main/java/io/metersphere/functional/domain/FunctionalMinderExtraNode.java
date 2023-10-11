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
public class FunctionalMinderExtraNode implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_minder_extra_node.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "父节点的ID，即模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.parent_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_minder_extra_node.parent_id.length_range}", groups = {Created.class, Updated.class})
    private String parentId;

    @Schema(description =  "项目ID，可扩展为其他资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.group_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_minder_extra_node.group_id.length_range}", groups = {Created.class, Updated.class})
    private String groupId;

    @Schema(description =  "存储脑图节点额外信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_minder_extra_node.node_data.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 2147483647, message = "{functional_minder_extra_node.node_data.length_range}", groups = {Created.class, Updated.class})
    private String nodeData;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        parentId("parent_id", "parentId", "VARCHAR", false),
        groupId("group_id", "groupId", "VARCHAR", false),
        nodeData("node_data", "nodeData", "LONGVARCHAR", false);

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