package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FunctionalCaseRelationshipDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联表id")
    private String id;

    @Schema(description = "用例名称")
    private String name;

    @Schema(description = "版本")
    private String versionId;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "创建人名称")
    private String userName;

    @Schema(description = "num")
    private String num;

    @Schema(description = "caseId")
    private String caseId;

}
