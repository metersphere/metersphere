package io.metersphere.system.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "测试资源池节点")
@Table("test_resource")
@Data
public class TestResource implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{test_resource.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "Test resource ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_resource.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_resource.test_resource_pool_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Test resource pool ID this test resource belongs to", required = true, allowableValues = "range[1, 50]")
    private String testResourcePoolId;


    @ApiModelProperty(name = "Test resource configuration", required = false, allowableValues = "range[1, ]")
    private byte[] configuration;

    @Size(min = 1, max = 64, message = "{test_resource.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_resource.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Test resource status", required = true, allowableValues = "range[1, 64]")
    private String status;


    @ApiModelProperty(name = "Create timestamp", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "Update timestamp", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


}