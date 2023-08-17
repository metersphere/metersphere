package io.metersphere.system.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProjectAddMemberBatchRequest extends ProjectAddMemberRequest{
    @Schema(description =  "项目ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<
            @NotBlank(message = "{project.id.not_blank}", groups = {Created.class, Updated.class})
                    String> projectIds;
}
