package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class TestResourcePool implements Serializable {
    @Schema(title = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_resource_pool.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{test_resource_pool.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "是否启用")
    private Boolean enable;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "是否用于接口测试")
    private Boolean apiTest;

    @Schema(title = "是否用于性能测试")
    private Boolean loadTest;

    @Schema(title = "是否用于ui测试")
    private Boolean uiTest;

    @Schema(title = "ms部署地址")
    private String serverUrl;

    @Schema(title = "资源池应用类型（组织/全部）")
    private Boolean allOrg;

    @Schema(title = "是否删除")
    private Boolean deleted;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        description("description", "description", "VARCHAR", false),
        enable("enable", "enable", "BIT", true),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        apiTest("api_test", "apiTest", "BIT", false),
        loadTest("load_test", "loadTest", "BIT", false),
        uiTest("ui_test", "uiTest", "BIT", false),
        serverUrl("server_url", "serverUrl", "VARCHAR", false),
        allOrg("all_org", "allOrg", "BIT", false),
        deleted("deleted", "deleted", "BIT", false);

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