package io.metersphere.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class CaseRelateBugDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "缺陷的id")
    private String bugId;

    @Schema(description = "中间表id")
    private String id;

    @Schema(description = "缺陷名称")
    private String title;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "用例id")
    private String caseId;

    @Schema(description = "num")
    private String num;

    @Schema(description = "status")
    private String status;
}
