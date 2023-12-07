package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class ReviewFunctionalCaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id")
    private String id;

    @Schema(description = "用例名称")
    private String name;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "评审人")
    private List<String> reviewers;

    @Schema(description = "评审人名称")
    private List<String> reviewNames;

    @Schema(description = "评审状态")
    private String status;

    @Schema(description = "所属模块")
    private String moduleId;

    @Schema(description = "模块名称")
    private String moduleName;

}
