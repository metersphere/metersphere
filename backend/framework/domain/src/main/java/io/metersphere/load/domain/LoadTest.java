package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTest implements Serializable {
    @Schema(title = "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{load_test.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "测试名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{load_test.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "状态为Error时表示错误信息")
    private String description;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "状态: Starting, Running, Completed, Error, etc.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{load_test.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.test_resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(title = "测试数字ID，例如: 100001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.num.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 10, message = "{load_test.num.length_range}", groups = {Created.class, Updated.class})
    private Integer num;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.pos.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 19, message = "{load_test.pos.length_range}", groups = {Created.class, Updated.class})
    private Long pos;

    @Schema(title = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(title = "基版本数据ID，首条测试和测试ID相同", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(title = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test.latest.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{load_test.latest.length_range}", groups = {Created.class, Updated.class})
    private Boolean latest;

    private static final long serialVersionUID = 1L;
}