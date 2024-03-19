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

    @Schema(description = "id")
    private String id;

    @Schema(description = "业务id")
    private String num;

    @Schema(description = "用例名称")
    private String name;

    @Schema(description = "用例id")
    private String caseId;

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

    @Schema(description = "编辑模式(用于脑图的查询)")
    private String caseEditType;

    @Schema(description = "用例创建人")
    private String createUser;

    @Schema(description = "用例创建人名称")
    private String createUserName;

    @Schema(description = "只看我的评审状态")
    private String myStatus;

    @Schema(description = "自定义字段集合")
    private List<FunctionalCaseCustomFieldDTO> customFields;

}
