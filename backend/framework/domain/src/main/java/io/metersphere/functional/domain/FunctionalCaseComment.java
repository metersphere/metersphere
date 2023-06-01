package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseComment implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_comment.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_comment.case_id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(title = "评论人")
    private String createUser;

    @Schema(title = "评论时添加的状态：通过/不通过/重新提审/通过标准变更标记/强制通过标记/强制不通过标记/状态变更标记")
    private String status;

    @Schema(title = "评论类型：用例评论/测试计划用例评论/评审用例评论", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_comment.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case_comment.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(title = "当前评审所属的测试计划ID或评审ID")
    private String belongId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;
}