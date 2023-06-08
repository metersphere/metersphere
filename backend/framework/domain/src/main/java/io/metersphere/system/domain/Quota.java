package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class Quota implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{quota.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{quota.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "组织ID")
    private String organizationId;

    @Schema(title = "项目类型配额")
    private String projectId;

    @Schema(title = "功能用例数量")
    private Integer functionalCase;

    @Schema(title = "总vum数")
    private BigDecimal loadTestVumTotal;

    @Schema(title = "消耗的vum数")
    private BigDecimal loadTestVumUsed;

    @Schema(title = "最大并发数")
    private Integer loadTestMaxThreads;

    @Schema(title = "最大执行时长")
    private Integer loadTestDuration;

    @Schema(title = "资源池列表")
    private String resourcePool;

    @Schema(title = "是否使用默认值")
    private Boolean useDefault;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "成员数量限制")
    private Integer member;

    @Schema(title = "项目数量限制")
    private Integer project;

    @Schema(title = "总vum数")
    private BigDecimal apiTestVumTotal;

    @Schema(title = "消耗的vum数")
    private BigDecimal apiTestVumUsed;

    @Schema(title = "总vum数")
    private BigDecimal uiTestVumTotal;

    @Schema(title = "消耗的vum数")
    private BigDecimal uiTestVumUsed;

    @Schema(title = "文件大小限制")
    private Long fileStorage;

    private static final long serialVersionUID = 1L;
}