package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioBatchEditRequest extends ApiScenarioBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
            @Size(min = 1, max = 50, message = "{api_test_case.tag.length_range}")
                    String> tags;
    @Schema(description = "批量编辑的类型  用例等级: Priority,状态 :Status,标签: Tags,用例环境: Environment")
    @NotBlank
    private String type;
    @Schema(description = "默认覆盖原标签")
    private boolean appendTag = false;
    @Schema(description = "环境id")
    private String envId;
    @Schema(description = "使用环境组")
    private boolean grouped = false;
    @Schema(description = "环境组id")
    private String groupId;
    @Schema(description = "用例状态")
    private String status;
    @Schema(description = "用例等级")
    private String priority;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }

}
