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
public class ApiMockBatchEditRequest extends ApiTestCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签")
    private LinkedHashSet<String> tags;
    @Schema(description = "批量编辑的类型 状态 :Status,标签: Tags")
    @NotBlank
    private String type;
    @Schema(description = "是否追加标签")
    private boolean append = false;
    @Schema(description = "默认不清空所有标签")
    private boolean clear = false;
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
