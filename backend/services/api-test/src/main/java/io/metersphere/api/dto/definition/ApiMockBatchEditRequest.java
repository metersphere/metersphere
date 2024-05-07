package io.metersphere.api.dto.definition;

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
public class ApiMockBatchEditRequest extends ApiTestCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
            @Size(min = 1, max = 64, message = "{api_test_case.tag.length_range}")
                    String> tags;
    @Schema(description = "批量编辑的类型 状态 :Status,标签: Tags")
    @NotBlank
    private String type;
    @Schema(description = "默认覆盖原标签")
    private boolean append = false;
    @Schema(description = "状态  开启/关闭")
    private boolean enable;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }

}
