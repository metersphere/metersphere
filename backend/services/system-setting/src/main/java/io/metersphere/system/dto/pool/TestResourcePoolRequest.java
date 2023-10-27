package io.metersphere.system.dto.pool;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestResourcePoolRequest {

    @Schema(description =  "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool.id.length_range}", groups = {Updated.class})
    private String id;

    @Schema(description =  "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool.name.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 255, message = "{test_resource_pool.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "描述")
    private String description;

    @Schema(description =  "类型", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = { "Node","Kubernetes"})
    @NotBlank(message = "{test_resource_pool.type.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 30, message = "{test_resource_pool.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description =  "是否启用")
    private Boolean enable;

    @Schema(description =  "是否用于接口测试")
    private Boolean apiTest;

    @Schema(description =  "是否用于性能测试")
    private Boolean loadTest;

    @Schema(description =  "是否用于ui测试")
    private Boolean uiTest;

    @Schema(description =  "ms部署地址")
    private String serverUrl;

    @Schema(description =  "资源池应用类型（组织/全部）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_resource_pool.all_org.not_blank}", groups = {Created.class, Updated.class})
    private Boolean allOrg;

    @Schema(description =  "其余配置")
    private TestResourceDTO  testResourceDTO;

}
