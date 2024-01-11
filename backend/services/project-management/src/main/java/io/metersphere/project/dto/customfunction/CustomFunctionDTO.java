package io.metersphere.project.dto.customfunction;

import io.metersphere.project.domain.CustomFunction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: LAN
 * @date: 2024/1/9 19:42
 * @version: 1.0
 */
@Data
public class CustomFunctionDTO extends CustomFunction {

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description =  "参数列表")
    private String params;

    @Schema(description =  "函数体")
    private String script;

    @Schema(description =  "执行结果")
    private String result;
}
