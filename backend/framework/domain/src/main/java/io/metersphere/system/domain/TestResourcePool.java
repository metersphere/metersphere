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

@ApiModel(value = "测试资源池")
@Table("test_resource_pool")
@Data
public class TestResourcePool implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{test_resource_pool.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "资源池ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 64, message = "{test_resource_pool.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_resource_pool.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "名称", required = true, allowableValues = "range[1, 64]")
    private String name;

    @Size(min = 1, max = 30, message = "{test_resource_pool.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_resource_pool.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "类型", required = true, allowableValues = "range[1, 30]")
    private String type;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 255]")
    private String description;

    @Size(min = 1, max = 64, message = "{test_resource_pool.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_resource_pool.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "状态", required = true, allowableValues = "range[1, 64]")
    private String status;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "性能测试镜像", required = false, allowableValues = "range[1, 100]")
    private String image;


    @ApiModelProperty(name = "性能测试jvm配置", required = false, allowableValues = "range[1, 200]")
    private String heap;


    @ApiModelProperty(name = "性能测试gc配置", required = false, allowableValues = "range[1, 200]")
    private String gcAlgo;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "是否用于接口测试", required = false, allowableValues = "range[1, 1]")
    private Boolean api;


    @ApiModelProperty(name = "是否用于性能测试", required = false, allowableValues = "range[1, 1]")
    private Boolean performance;


}