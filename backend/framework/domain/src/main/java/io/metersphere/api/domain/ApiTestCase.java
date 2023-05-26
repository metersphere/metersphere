package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCase implements Serializable {
    @Schema(title = "接口用例pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_test_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "接口用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_test_case.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "更新人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.deleted.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_test_case.deleted.length_range}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(title = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.priority.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}", groups = {Created.class, Updated.class})
    private String priority;

    @Schema(title = "接口用例编号id")
    private Integer num;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_test_case.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "最新执行结果状态")
    private String apiReportStatus;

    @Schema(title = "最后执行结果报告fk")
    private String apiReportId;

    @Schema(title = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.pos.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{api_test_case.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "是否开启同步", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.sync_enable.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{api_test_case.sync_enable.length_range}", groups = {Created.class, Updated.class})
    private Boolean syncEnable;

    @Schema(title = "需要同步的开始时间")
    private Long syncTime;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    private String apiDefinitionId;

    @Schema(title = "版本fk")
    private String versionId;

    @Schema(title = "责任人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.principal.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(title = "环境fk")
    private String environmentId;

    private static final long serialVersionUID = 1L;
}