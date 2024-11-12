package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserStatusCountDTO {

    @Schema(description =  "用户ID")
    private String userId;
    @Schema(description =  "状态")
    private String status;
    @Schema(description =  "数量")
    private int count;
}
