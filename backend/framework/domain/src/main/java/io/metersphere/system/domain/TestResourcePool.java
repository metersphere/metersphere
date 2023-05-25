package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestResourcePool implements Serializable {
    @Schema(title = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{test_resource_pool.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 64]")
    @NotBlank(message = "{test_resource_pool.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{test_resource_pool.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "类型", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 30]")
    @NotBlank(message = "{test_resource_pool.type.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 30, message = "{test_resource_pool.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 64]")
    @NotBlank(message = "{test_resource_pool.status.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{test_resource_pool.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "性能测试镜像")
    private String image;

    @Schema(title = "性能测试jvm配置")
    private String heap;

    @Schema(title = "性能测试gc配置")
    private String gcAlgo;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "是否用于接口测试")
    private Boolean api;

    @Schema(title = "是否用于性能测试")
    private Boolean performance;

    private static final long serialVersionUID = 1L;
}