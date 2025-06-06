package io.metersphere.system.domain;

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
public class AiModelSource implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ai_model_source.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "模型类别（大语言/视觉/音频）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ai_model_source.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "模型供应商", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.provider_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.provider_name.length_range}", groups = {Created.class, Updated.class})
    private String providerName;

    @Schema(description = "模型类型（公有/私有）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.permission_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ai_model_source.permission_type.length_range}", groups = {Created.class, Updated.class})
    private String permissionType;

    @Schema(description = "模型连接状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ai_model_source.status.not_blank}", groups = {Created.class})
    private Boolean status;

    @Schema(description = "模型拥有者", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.owner.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.owner.length_range}", groups = {Created.class, Updated.class})
    private String owner;

    @Schema(description = "模型拥有者类型（个人/企业）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.owner_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.owner_type.length_range}", groups = {Created.class, Updated.class})
    private String ownerType;

    @Schema(description = "基础名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.base_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.base_name.length_range}", groups = {Created.class, Updated.class})
    private String baseName;

    @Schema(description = "模型key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.app_key.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.app_key.length_range}", groups = {Created.class, Updated.class})
    private String appKey;

    @Schema(description = "模型url", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.api_url.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.api_url.length_range}", groups = {Created.class, Updated.class})
    private String apiUrl;

    @Schema(description = "模型参数配置值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ai_model_source.adv_settings.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{ai_model_source.adv_settings.length_range}", groups = {Created.class, Updated.class})
    private String advSettings;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人(操作人）")
    private String createUser;


    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        type("type", "type", "VARCHAR", true),
        providerName("provider_name", "providerName", "VARCHAR", false),
        permissionType("permission_type", "permissionType", "VARCHAR", false),
        status("status", "status", "BIT", true),
        owner("owner", "owner", "VARCHAR", true),
        ownerType("owner_type", "ownerType", "VARCHAR", false),
        baseName("base_name", "baseName", "VARCHAR", false),
        appKey("app_key", "appKey", "VARCHAR", false),
        apiUrl("api_url", "apiUrl", "VARCHAR", false),
        advSettings("adv_settings", "advSettings", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        createUser("create_user", "createUser", "VARCHAR", false);

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