package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiDefinitionMock;
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
public class ApiDefinitionMockDTO extends ApiDefinitionMock {

    @Schema(description = "请求内容")
    private AbstractMsTestElement matching;

    @Schema(description = "响应内容")
    private List<HttpResponse> response;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "接口编号")
    private Long apiNum;

    @Schema(description = "接口名称")
    private String apiName;

    @Schema(description = "接口路径")
    private String apiPath;

    @Schema(description = "接口类型")
    private String apiMethod;

}
