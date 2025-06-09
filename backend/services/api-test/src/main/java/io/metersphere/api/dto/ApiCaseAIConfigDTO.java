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


    public ApiCaseAIConfigDTO() {
        // 默认构造函数
        this.normal = true; // 默认生成正常场景
        this.abnormal = true; // 默认生成异常场景
        this.caseName = true; // 默认生成用例名称
        this.requestParams = true; // 默认生成请求参数
        this.preScript = true; // 默认生成前置脚本
        this.postScript = true; // 默认生成后置脚本
        this.assertion = true; // 默认生成断言
    }
}
