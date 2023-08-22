package io.metersphere.sdk.dto.environment.assertions.document;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DocumentElement {
    @Schema(description = "id")
    private String id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "包含")
    private Boolean include = false;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "类型校验")
    private boolean typeVerification;
    @Schema(description = "类型 object/array/string/number/boolean/integer")
    private String type;
    @Schema(description = "组id")
    private String groupId;
    @Schema(description = "行数")
    private int rowspan;
    @Schema(description = "校验组内元素")
    private boolean arrayVerification;
    @Schema(description = "内容校验  none/value_eq/value_not_eq/value_in/length_eq/length_not_eq/length_gt/length_lt/regular")
    private String contentVerification;
    @Schema(description = "预期结果")
    private Object expectedOutcome;
    @Schema(description = "子级")
    private List<DocumentElement> children;

}
