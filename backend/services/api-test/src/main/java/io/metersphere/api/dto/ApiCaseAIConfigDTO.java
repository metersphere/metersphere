package io.metersphere.api.dto;

import lombok.Data;

@Data
public class ApiCaseAIConfigDTO {
    //正常场景
    private Boolean normal;
    //异常场景
    private Boolean abnormal;
    //用例名称
    private Boolean caseName;
    //请求参数
    private Boolean requestParams;
    //前置脚本
    private Boolean preScript;
    //后置脚本
    private Boolean postScript;
    //断言
    private Boolean assertion;

    public ApiCaseAIConfigDTO(Boolean normal, Boolean abnormal, Boolean caseName, Boolean requestParams, Boolean preScript, Boolean postScript, Boolean assertion) {
        this.normal = normal;
        this.abnormal = abnormal;
        this.caseName = caseName;
        this.requestParams = requestParams;
        this.preScript = preScript;
        this.postScript = postScript;
        this.assertion = assertion;
    }

    public ApiCaseAIConfigDTO() {
    }
}
