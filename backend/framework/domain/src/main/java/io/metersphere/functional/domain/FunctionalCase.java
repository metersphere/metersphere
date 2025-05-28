package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Data
public class FunctionalCase implements Serializable {
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "业务ID")
    private Long num;

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.template_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{functional_case.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "评审结果：未评审/评审中/通过/不通过/重新提审", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.review_status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case.review_status.length_range}", groups = {Created.class, Updated.class})
    private String reviewStatus;

    @Schema(description = "标签（JSON)")
    private java.util.List<String> tags;

    @Schema(description = "编辑模式：步骤模式/文本模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.case_edit_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.case_edit_type.length_range}", groups = {Created.class, Updated.class})
    private String caseEditType;

    @Schema(description = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description = "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(description = "最近的执行结果：未执行/通过/失败/阻塞/跳过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.last_execute_result.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case.last_execute_result.length_range}", groups = {Created.class, Updated.class})
    private String lastExecuteResult;

    @Schema(description = "是否在回收站：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "是否是ai自动生成的用例：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case.ai_create.not_blank}", groups = {Created.class})
    private Boolean aiCreate;

    @Schema(description = "是否是公共用例：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case.public_case.not_blank}", groups = {Created.class})
    private Boolean publicCase;

    @Schema(description = "是否为最新版本：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case.latest.not_blank}", groups = {Created.class})
    private Boolean latest;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    private static final long serialVersionUID = 1L;

    public enum Column {
        id("id", "id", "VARCHAR", false),
        num("num", "num", "BIGINT", false),
        moduleId("module_id", "moduleId", "VARCHAR", false),
        projectId("project_id", "projectId", "VARCHAR", false),
        templateId("template_id", "templateId", "VARCHAR", false),
        name("name", "name", "VARCHAR", true),
        reviewStatus("review_status", "reviewStatus", "VARCHAR", false),
        tags("tags", "tags", "VARCHAR", false),
        caseEditType("case_edit_type", "caseEditType", "VARCHAR", false),
        pos("pos", "pos", "BIGINT", false),
        versionId("version_id", "versionId", "VARCHAR", false),
        refId("ref_id", "refId", "VARCHAR", false),
        lastExecuteResult("last_execute_result", "lastExecuteResult", "VARCHAR", false),
        deleted("deleted", "deleted", "BIT", false),
        aiCreate("ai_create", "aiCreate", "BIT", false),
        publicCase("public_case", "publicCase", "BIT", false),
        latest("latest", "latest", "BIT", false),
        createUser("create_user", "createUser", "VARCHAR", false),
        updateUser("update_user", "updateUser", "VARCHAR", false),
        deleteUser("delete_user", "deleteUser", "VARCHAR", false),
        createTime("create_time", "createTime", "BIGINT", false),
        updateTime("update_time", "updateTime", "BIGINT", false),
        deleteTime("delete_time", "deleteTime", "BIGINT", false);

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