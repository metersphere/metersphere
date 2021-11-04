package io.metersphere.api.dto.scenario.environment;

import lombok.Data;
import java.util.List;

@Data
public class GlobalScriptConfig {
    //要过滤的请求
    private List<String> filterRequestPreScript;
    private List<String> filterRequestPostScript;
    //是否在请求自有脚本之后再执行
    private boolean isPreScriptExecAfterPrivateScript;
    private boolean isPostScriptExecAfterPrivateScript;
    //是否统计到场景中
    private boolean connScenarioPreScript;
    private boolean connScenarioPostScript;
}
