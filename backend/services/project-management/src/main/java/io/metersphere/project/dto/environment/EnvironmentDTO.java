package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvironmentDTO {

    @Schema(description = "环境ID")
    private String id;

    @Schema(description = "环境名称")
    private String name;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "是否是mock环境")
    private Boolean mock;

    @Schema(description = "是否是环境组")
    private Boolean isGroup;



}
