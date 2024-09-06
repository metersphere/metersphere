package io.metersphere.request;

import io.metersphere.sdk.dto.BaseCondition;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssociateOtherCaseRequest {

    @Schema(description = "不处理的ID")
    List<String> excludeIds;

    @Schema(description = "选择的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<
            @NotBlank(message = "{id must not be blank}", groups = {Created.class, Updated.class})
                    String
            > selectIds;

    @Schema(description = "是否选择所有数据")
    private boolean selectAll;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "版本来源")
    private String refId;

    @Schema(description = "要关联的用例选择的项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{associate_other_case_request.project_id.not_blank}")
    private String projectId;

    @Schema(description = "关联关系表里主ID eg:功能用例关联接口用例时为功能用例id")
    @NotBlank(message = "{associate_other_case_request.case_id.not_blank}")
    private String sourceId;

    @Schema(description = "接口用例的接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();

    @Schema(description = "关联用例的类型(API,SCENARIO,UI,PERFORMANCE)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{associate_other_case_request.type.not_blank}")
    private String sourceType;

    @Schema(description = "接口id")
    private String apiDefinitionId;

    @Schema(description = "查询条件")
    private BaseCondition condition = new BaseCondition();
}
