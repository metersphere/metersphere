package io.metersphere.load.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "性能测试用例")
@TableName("load_test")
@Data
public class LoadTest implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{load_test.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{load_test.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 255, message = "{load_test.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试名称", required = true, allowableValues = "range[1, 255]")
    private String name;


    @ApiModelProperty(name = "状态为Error时表示错误信息", required = false, allowableValues = "range[1, 500]")
    private String description;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 64, message = "{load_test.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "状态: Starting, Running, Completed, Error, etc.", required = true, allowableValues = "range[1, 64]")
    private String status;

    @Size(min = 1, max = 50, message = "{load_test.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.test_resource_pool_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源池ID", required = true, allowableValues = "range[1, 50]")
    private String testResourcePoolId;


    @ApiModelProperty(name = "测试数字ID，例如: 100001", required = true, dataType = "Integer")
    private Integer num;

    @Size(min = 1, max = 100, message = "{load_test.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "自定义排序，间隔5000", required = true, dataType = "Long")
    private Long pos;

    @Size(min = 1, max = 50, message = "{load_test.version_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.version_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "版本ID", required = true, allowableValues = "range[1, 50]")
    private String versionId;

    @Size(min = 1, max = 50, message = "{load_test.ref_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.ref_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "基版本数据ID，首条测试和测试ID相同", required = true, allowableValues = "range[1, 50]")
    private String refId;

    @Size(min = 1, max = 1, message = "{load_test.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test.latest.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否为最新版本 0:否，1:是", required = true, allowableValues = "range[1, 1]")
    private Boolean latest;


}