package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ProjectExtend implements Serializable {
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_extend.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project_extend.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "")
    private String tapdId;

    @Schema(title = "")
    private String jiraKey;

    @Schema(title = "")
    private String zentaoId;

    @Schema(title = "")
    private String azureDevopsId;

    @Schema(title = "用例模版ID")
    private String caseTemplateId;

    @Schema(title = "azure 过滤需求的 parent workItem ID")
    private String azureFilterId;

    @Schema(title = "项目使用哪个平台的模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_extend.platform.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 20, message = "{project_extend.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(title = "是否使用第三方平台缺陷模板")
    private Boolean thirdPartTemplate;

    @Schema(title = "是否开启版本管理")
    private Boolean versionEnable;

    @Schema(title = "")
    private String apiTemplateId;

    @Schema(title = "")
    private byte[] issueConfig;

    private static final long serialVersionUID = 1L;
}