package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestResource implements Serializable {
    @Schema(title = "资源节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_resource.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource.test_resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_resource.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(title = "资源节点状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_resource.enable.not_blank}", groups = {Created.class})
    private Boolean enable;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "资源节点配置")
    private byte[] configuration;

    @Schema(title = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_resource_pool.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    private static final long serialVersionUID = 1L;
}