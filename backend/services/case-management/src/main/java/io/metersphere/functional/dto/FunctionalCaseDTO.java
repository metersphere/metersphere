package io.metersphere.functional.dto;

import io.metersphere.system.dto.sdk.FunctionalCaseMessageDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FunctionalCaseDTO extends FunctionalCaseMessageDTO {

    @Schema(description =  "id")
    private String id;

    @Schema(description =  "项目ID")
    private String projectId;

    @Schema(description =  "评论@的人, 多个以';'隔开")
    private String relatedUsers;

    @Schema(description =  "自定义字段的值")
    private List<OptionDTO> fields;

    @Schema(description =  "触发方式：功能用例执行相关（测试计划/定时任务/用例评审）")
    private String triggerMode;

    @Schema(description = "message.follow_people")
    private List<String> followUsers;

}
