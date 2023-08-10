package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTest implements Serializable {
    @Schema(description =  "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "测试名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{load_test.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "状态为Error时表示错误信息")
    private String description;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "状态: Starting, Running, Completed, Error, etc.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{load_test.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.test_resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(description =  "测试数字ID，例如: 100001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{load_test.num.not_blank}", groups = {Created.class})
    private Integer num;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{load_test.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description =  "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description =  "基版本数据ID，首条测试和测试ID相同", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(description =  "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{load_test.latest.not_blank}", groups = {Created.class})
    private Boolean latest;

    private static final long serialVersionUID = 1L;
}