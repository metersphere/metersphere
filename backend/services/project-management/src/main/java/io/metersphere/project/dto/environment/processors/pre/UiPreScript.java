package io.metersphere.project.dto.environment.processors.pre;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UiPreScript {

    @Schema(description = "脚本执行顺序 true:先执行 false:后执行")
    private Boolean preScriptExecBefore = true;
    @Schema(description = "脚本类型 true:同步 false:异步")
    private Boolean jsrType = true;
    @Schema(description = "设置变量 true:有返回值 false:无返回值")
    private Boolean jsrSetVariable = true;
    @Schema(description = "变量名称")
    private String variableName;
    @Schema(description = "脚本内容")
    private Boolean value;

}
