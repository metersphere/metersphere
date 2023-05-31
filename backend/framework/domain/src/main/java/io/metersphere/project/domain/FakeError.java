package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FakeError implements Serializable {
    @Schema(title = "误报ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{fake_error.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{fake_error.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "更新人")
    private String updateUser;

    @Schema(title = "错误码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.error_code.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{fake_error.error_code.length_range}", groups = {Created.class, Updated.class})
    private String errorCode;

    @Schema(title = "匹配类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.match_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{fake_error.match_type.length_range}", groups = {Created.class, Updated.class})
    private String matchType;

    @Schema(title = "状态")
    private Boolean status;

    private static final long serialVersionUID = 1L;
}