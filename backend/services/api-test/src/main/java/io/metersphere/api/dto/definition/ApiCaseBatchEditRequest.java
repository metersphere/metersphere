package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiCaseBatchEditRequest extends ApiTestCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
                    String> tags;
    @Schema(description = "批量编辑的类型  用例等级: Priority,状态 :Status,标签: Tags,用例环境: Environment")
    @NotBlank
    private String type;
    @Schema(description = "默认覆盖原标签")
    private boolean appendTag = false;
    @Schema(description = "环境id")
    private String envId;
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
