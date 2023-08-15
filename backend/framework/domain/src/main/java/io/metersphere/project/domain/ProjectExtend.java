package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

@Data
public class ProjectExtend implements Serializable {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_extend.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_extend.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "")
    private String tapdId;

    @Schema(description = "")
    private String jiraKey;

    @Schema(description = "")
    private String zentaoId;

    @Schema(description = "")
    private String azureDevopsId;

    @Schema(description = "用例模版ID")
    private String caseTemplateId;

    @Schema(description = "azure 过滤需求的 parent workItem ID")
    private String azureFilterId;

    @Schema(description = "项目使用哪个平台的模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_extend.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{project_extend.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(description = "是否使用第三方平台缺陷模板")
    private Boolean thirdPartTemplate;

    @Schema(description = "是否开启版本管理")
    private Boolean versionEnable;

    @Schema(description = "")
    private String issueConfig;

    @Schema(description = "")
    private String apiTemplateId;

    @Schema(description = "模块设置")
    private String moduleSetting;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        tapdId("tapd_id", "tapdId", "VARCHAR", false),
        jiraKey("jira_key", "jiraKey", "VARCHAR", false),
        zentaoId("zentao_id", "zentaoId", "VARCHAR", false),
        azureDevopsId("azure_devops_id", "azureDevopsId", "VARCHAR", false),
        caseTemplateId("case_template_id", "caseTemplateId", "VARCHAR", false),
        azureFilterId("azure_filter_id", "azureFilterId", "VARCHAR", false),
        platform("platform", "platform", "VARCHAR", false),
        thirdPartTemplate("third_part_template", "thirdPartTemplate", "BIT", false),
        versionEnable("version_enable", "versionEnable", "BIT", false),
        issueConfig("issue_config", "issueConfig", "VARCHAR", false),
        apiTemplateId("api_template_id", "apiTemplateId", "VARCHAR", false),
        moduleSetting("module_setting", "moduleSetting", "VARCHAR", false);

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