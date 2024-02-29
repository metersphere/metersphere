package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class FunctionalCaseDemand implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_demand.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_demand.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description = "父需求id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.parent.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{functional_case_demand.parent.length_range}", groups = {Created.class, Updated.class})
    private String parent;

    @Schema(description = "是否与父节点一起关联：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case_demand.with_parent.not_blank}", groups = {Created.class})
    private Boolean withParent;

    @Schema(description = "需求ID")
    private String demandId;

    @Schema(description = "需求标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.demand_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{functional_case_demand.demand_name.length_range}", groups = {Created.class, Updated.class})
    private String demandName;

    @Schema(description = "需求地址")
    private String demandUrl;

    @Schema(description = "需求所属平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_demand.demand_platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case_demand.demand_platform.length_range}", groups = {Created.class, Updated.class})
    private String demandPlatform;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新人")
    private String updateUser;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        caseId("case_id", "caseId", "VARCHAR", false),
        parent("parent", "parent", "VARCHAR", false),
        withParent("with_parent", "withParent", "BIT", false),
        demandId("demand_id", "demandId", "VARCHAR", false),
        demandName("demand_name", "demandName", "VARCHAR", false),
        demandUrl("demand_url", "demandUrl", "VARCHAR", false),
        demandPlatform("demand_platform", "demandPlatform", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false);

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