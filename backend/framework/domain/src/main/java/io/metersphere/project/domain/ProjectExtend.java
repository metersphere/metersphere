package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Data;

@Data
public class ProjectExtend implements Serializable {
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_extend.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_extend.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "")
    private String tapdId;

    @Schema(title = "")
    private String jiraKey;

    @Schema(title = "")
    private String zentaoId;

    @Schema(title = "")
    private String azureDevopsId;

    @Schema(title = "用例模版ID")
    private String caseTemplateId;

    @Schema(title = "azure 过滤需求的 parent workItem ID")
    private String azureFilterId;

    @Schema(title = "项目使用哪个平台的模板")
    private String platform;

    @Schema(title = "是否使用第三方平台缺陷模板")
    private Boolean thirdPartTemplate;

    @Schema(title = "是否开启版本管理")
    private Boolean versionEnable;

    @Schema(title = "")
    private String issueConfig;

    @Schema(title = "")
    private String apiTemplateId;

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
        apiTemplateId("api_template_id", "apiTemplateId", "VARCHAR", false);

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