package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCountDTO {

    @Schema(description =  "项目id")
    private String projectId;

    @Schema(description =  "数量")
    private int count;

}
