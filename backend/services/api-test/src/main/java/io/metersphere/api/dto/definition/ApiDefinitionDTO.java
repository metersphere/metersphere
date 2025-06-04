package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionDTO extends ApiDefinition{

    @Schema(description = "请求内容")
    private AbstractMsTestElement request;

    @Schema(description = "响应内容")
    private List<HttpResponse> response;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "删除人名称")
    private String deleteUserName;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "用例数")
    private int caseTotal;

    @Schema(description = "用例通过率")
    private String casePassRate;

    @Schema(description = "用例执行结果")
    private String caseStatus;

    @Schema(description = "是否关注")
    private Boolean follow;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "自定义字段集合")
    private List<ApiDefinitionCustomFieldDTO> customFields;

}
