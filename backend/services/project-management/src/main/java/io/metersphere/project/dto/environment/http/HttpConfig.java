package io.metersphere.project.dto.environment.http;


import io.metersphere.project.dto.environment.KeyValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class HttpConfig implements Serializable {
    @Schema(description = "环境域名")
    private String socket;
    @Schema(description = "domain")
    private String domain;
    @Schema(description = "协议")
    private String protocol = "https";
    @Schema(description = "应用模块（接口测试、ui测试）")
    private List<ApplicationModule> applicationModule;
    @Schema(description = "启用条件  NONE/MODULE/PATH")
    private String type;
    @Schema(description = "启用条件为PATH时，需要填写的路径/ 如果是模块时需要时模块的id")
    private List<KeyValue> details;
    @Schema(description = "请求头")
    private List<KeyValue> headers;
    @Schema(description = "浏览器 选项为chrome/firefox")
    private String browser;
    private String description;

    @Serial
    private static final long serialVersionUID = 1L;

}
