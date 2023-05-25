package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口用例")
@Table("api_test_case")
@Data
public class ApiTestCase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_test_case.id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口用例pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 200, message = "{api_test_case.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.name.not_blank}", groups = {Created.class})
    @Schema(title = "接口用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Size(min = 1, max = 50, message = "{api_test_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_test_case.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "更新人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;

    @Schema(title = "删除时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long deleteTime;

    @Schema(title = "删除人", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String deleteUser;

    @Size(min = 1, max = 1, message = "{api_test_case.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.deleted.not_blank}", groups = {Created.class})
    @Schema(title = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.priority.not_blank}", groups = {Created.class})
    @Schema(title = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    private String priority;

    @Schema(title = "接口用例编号id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer num;

    @Schema(title = "标签", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tags;

    @Schema(title = "用例状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @Schema(title = "最新执行结果状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String apiReportStatus;

    @Schema(title = "最后执行结果报告fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String apiReportId;

    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;

    @Size(min = 1, max = 1, message = "{api_test_case.sync_enable.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.sync_enable.not_blank}", groups = {Created.class})
    @Schema(title = "是否开启同步", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean syncEnable;

    @Schema(title = "需要同步的开始时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long syncTime;

    @Size(min = 1, max = 50, message = "{api_test_case.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}", groups = {Created.class})
    @Schema(title = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiDefinitionId;

    @Schema(title = "版本fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String versionId;

    @Size(min = 1, max = 50, message = "{api_test_case.principal.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.principal.not_blank}", groups = {Created.class})
    @Schema(title = "责任人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String principal;

    @Schema(title = "环境fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environmentId;

}