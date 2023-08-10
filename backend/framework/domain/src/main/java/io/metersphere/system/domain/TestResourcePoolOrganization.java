package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestResourcePoolOrganization implements Serializable {
    @Schema(description =  "测试资源池项目关系ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool_organization.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool_organization.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool_organization.test_resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool_organization.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool_organization.org_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool_organization.org_id.length_range}", groups = {Created.class, Updated.class})
    private String orgId;

    private static final long serialVersionUID = 1L;
}