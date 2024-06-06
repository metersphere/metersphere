package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.dto.mockserver.MockMatchRule;
import io.metersphere.api.dto.mockserver.MockResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionMockDTO extends ApiDefinitionMock {

    @Schema(description = "请求内容")
    private MockMatchRule mockMatchRule;

    @Schema(description = "响应内容")
    private MockResponse response;

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
    @Schema(description = "接口协议")
    private String protocol;

}
