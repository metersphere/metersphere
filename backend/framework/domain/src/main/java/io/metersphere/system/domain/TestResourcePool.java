package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestResourcePool implements Serializable {
    @Schema(title = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_resource_pool.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "类型(node/k8s)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{test_resource_pool.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "是否启用")
    private Boolean enable;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "是否用于接口测试")
    private Boolean apiTest;

    @Schema(title = "是否用于性能测试")
    private Boolean loadTest;

    @Schema(title = "是否用于ui测试")
    private Boolean uiTest;

    @Schema(title = "ms部署地址")
    private String serverUrl;

    @Schema(title = "资源池应用类型（组织/全部）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_resource_pool.all_org.not_blank}", groups = {Created.class})
    private Boolean allOrg;

    @Schema(title = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_resource_pool.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    private static final long serialVersionUID = 1L;
}