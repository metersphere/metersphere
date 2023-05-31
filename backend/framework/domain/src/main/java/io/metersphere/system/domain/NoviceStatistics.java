package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class NoviceStatistics implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{novice_statistics.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{novice_statistics.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用户id")
    private String userId;

    @Schema(title = "新手引导完成的步骤", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{novice_statistics.guide_step.not_blank}", groups = {Created.class})
    private Boolean guideStep;

    @Schema(title = "新手引导的次数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{novice_statistics.guide_num.not_blank}", groups = {Created.class})
    private Integer guideNum;

    @Schema(title = "")
    private Long createTime;

    @Schema(title = "")
    private Long updateTime;

    @Schema(title = "data option (JSON format)")
    private byte[] dataOption;

    private static final long serialVersionUID = 1L;
}