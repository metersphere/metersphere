package io.metersphere.project.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EnvironmentGroupDTO implements Serializable {
    private String id;

    @Schema(description = "环境组名")
    private String name;

    @Schema(description = "所属项目id")
    private String projectId;

    @Schema(description = "环境组描述")
    private String description;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "自定义排序")
    private Long pos;

    @Schema(description = "环境组详情")
    private List<EnvironmentGroupInfo> environmentGroupInfo;

}