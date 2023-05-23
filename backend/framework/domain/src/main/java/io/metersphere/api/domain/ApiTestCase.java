
package io.metersphere.api.domain;

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

@ApiModel(value = "接口用例")
@Table("api_test_case")
@Data
public class ApiTestCase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_test_case.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口用例pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 200, message = "{api_test_case.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口用例名称", required = true, allowableValues = "range[1, 200]")
    private String name;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{api_test_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_test_case.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "更新人", required = true, allowableValues = "range[1, 50]")
    private String updateUser;

    @ApiModelProperty(name = "删除时间", required = false, dataType = "Long")
    private Long deleteTime;

    @ApiModelProperty(name = "删除人", required = false, allowableValues = "range[1, 50]")
    private String deleteUser;

    @Size(min = 1, max = 1, message = "{api_test_case.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除标识", required = true, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.priority.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用例等级", required = true, allowableValues = "range[1, 50]")
    private String priority;

    @ApiModelProperty(name = "接口用例编号id", required = false, dataType = "Integer")
    private Integer num;

    @ApiModelProperty(name = "标签", required = false, allowableValues = "range[1, 500]")
    private String tags;

    @ApiModelProperty(name = "用例状态", required = false, allowableValues = "range[1, 20]")
    private String status;

    @ApiModelProperty(name = "最新执行结果状态", required = false, allowableValues = "range[1, 20]")
    private String apiReportStatus;

    @ApiModelProperty(name = "最后执行结果报告fk", required = false, allowableValues = "range[1, 50]")
    private String apiReportId;

    @ApiModelProperty(name = "自定义排序", required = true, dataType = "Long")
    private Long pos;

    @Size(min = 1, max = 1, message = "{api_test_case.sync_enable.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.sync_enable.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否开启同步", required = true, allowableValues = "range[1, 1]")
    private Boolean syncEnable;

    @ApiModelProperty(name = "需要同步的开始时间", required = false, dataType = "Long")
    private Long syncTime;

    @Size(min = 1, max = 50, message = "{api_test_case.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口fk", required = true, allowableValues = "range[1, 50]")
    private String apiDefinitionId;

    @ApiModelProperty(name = "版本fk", required = false, allowableValues = "range[1, 50]")
    private String versionId;

    @Size(min = 1, max = 50, message = "{api_test_case.principal.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case.principal.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "责任人", required = true, allowableValues = "range[1, 50]")
    private String principal;

    @ApiModelProperty(name = "环境fk", required = false, allowableValues = "range[1, 50]")
    private String environmentId;

}