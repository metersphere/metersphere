package io.metersphere.functional.domain;


import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例")
@Table("functional_case")
@Data
public class FunctionalCase implements Serializable {
    private static final long serialVersionUID = 1L;


    @Id
    @NotBlank(message = "{functional_case.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;


    @Schema(title = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer num;

    @Size(min = 1, max = 64, message = "{functional_case.custom_num.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.custom_num.not_blank}", groups = {Created.class})
    @Schema(title = "自定义业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customNum;

    @Size(min = 1, max = 50, message = "{functional_case.module_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.module_id.not_blank}", groups = {Created.class})
    @Schema(title = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleId;

    @Size(min = 1, max = 50, message = "{functional_case.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Size(min = 1, max = 255, message = "{functional_case.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.name.not_blank}", groups = {Created.class})
    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(min = 1, max = 64, message = "{functional_case.review_status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.review_status.not_blank}", groups = {Created.class})
    @Schema(title = "评审状态：未开始/进行中/已完成/已结束", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewStatus;


    @Schema(title = "标签（JSON)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tags;

    @Size(min = 1, max = 64, message = "{functional_case.step_model.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.step_model.not_blank}", groups = {Created.class})
    @Schema(title = "编辑模式：步骤模式/文本模式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stepModel;


    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;

    @Size(min = 1, max = 50, message = "{functional_case.version_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.version_id.not_blank}", groups = {Created.class})
    @Schema(title = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String versionId;

    @Size(min = 1, max = 50, message = "{functional_case.ref_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.ref_id.not_blank}", groups = {Created.class})
    @Schema(title = "指向初始版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refId;

    @Size(min = 1, max = 64, message = "{functional_case.last_execute_result.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.last_execute_result.not_blank}", groups = {Created.class})
    @Schema(title = "最近的执行结果：未执行/通过/失败/阻塞/跳过", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastExecuteResult;

    @Size(min = 1, max = 1, message = "{functional_case.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.deleted.not_blank}", groups = {Created.class})
    @Schema(title = "是否在回收站：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean deleted;

    @Size(min = 1, max = 1, message = "{functional_case.public_case.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.public_case.not_blank}", groups = {Created.class})
    @Schema(title = "是否是公共用例：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean publicCase;

    @Size(min = 1, max = 1, message = "{functional_case.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.latest.not_blank}", groups = {Created.class})
    @Schema(title = "是否为最新版本：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean latest;

    @Size(min = 1, max = 100, message = "{functional_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;


    @Schema(title = "删除人", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String deleteUser;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;


    @Schema(title = "删除时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long deleteTime;
}
