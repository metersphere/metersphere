package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCase implements Serializable {
    @Schema(description =  "接口用例pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_test_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "接口用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_test_case.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "更新人")
    private String updateUser;

    @Schema(description =  "删除时间")
    private Long deleteTime;

    @Schema(description =  "删除人")
    private String deleteUser;

    @Schema(description =  "删除标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description =  "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.priority.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}", groups = {Created.class, Updated.class})
    private String priority;

    @Schema(description =  "接口用例编号id")
    private Integer num;

    @Schema(description =  "标签")
    private String tags;

    @Schema(description =  "用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_test_case.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "最新执行结果状态")
    private String apiReportStatus;

    @Schema(description =  "最后执行结果报告fk")
    private String apiReportId;

    @Schema(description =  "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description =  "是否开启同步", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_test_case.sync_enable.not_blank}", groups = {Created.class})
    private Boolean syncEnable;

    @Schema(description =  "需要同步的开始时间")
    private Long syncTime;

    @Schema(description =  "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    private String apiDefinitionId;

    @Schema(description =  "版本fk")
    private String versionId;

    @Schema(description =  "责任人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.principal.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.principal.length_range}", groups = {Created.class, Updated.class})
    private String principal;

    @Schema(description =  "环境fk")
    private String environmentId;

    private static final long serialVersionUID = 1L;
}