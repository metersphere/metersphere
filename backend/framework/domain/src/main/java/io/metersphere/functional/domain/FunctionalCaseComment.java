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

@Schema(title = "功能用例评论")
@Table("functional_case_comment")
@Data
public class FunctionalCaseComment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{functional_case_comment.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_comment.case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_comment.case_id.not_blank}", groups = {Created.class})
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String caseId;

    @Size(min = 1, max = 50, message = "{functional_case_comment.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_comment.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "评论人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;


    @Schema(title = "评论时添加的状态：通过/不通过/重新提审/通过标准变更标记/强制通过标记/强制不通过标记/状态变更标记", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 64]")
    private String status;

    @Size(min = 1, max = 64, message = "{functional_case_comment.type.length_range}", groups = {Created.class, Updated.class})

    @NotBlank(message = "{functional_case_comment.type.not_blank}", groups = {Created.class})

    @Schema(title = "评论类型：用例评论/测试计划用例评论/评审用例评论", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 64]")
    private String type;


    @Schema(title = "当前评审所属的测试计划ID或评审ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String belongId;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;


    @Schema(title = "描述", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;
}
