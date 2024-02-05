package io.metersphere.project.dto.environment.http;


import io.metersphere.project.dto.environment.KeyValue;
import io.metersphere.project.api.KeyValueEnableParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class HttpConfig implements Serializable {
    @Schema(description = "环境域名")
    private String url;
    @Schema(description = "启用条件  NONE/MODULE/PATH")
    private String type = "NONE";
    @Schema(description = "启用条件为PATH时，需要填写的路径/  key为equal时，value为路径，key为contain时，value为包含的路径")
    private List<KeyValue> details;
    @Schema(description = "启用条件为MODULE时，需要模块的id")
    private List<String> moduleIds;
    @Schema(description = "请求头")
    private List<KeyValueEnableParam> headers;
    @Schema(description = "浏览器 选项为chrome/firefox")
    private String browser;
    private String description;

    @Serial
    private static final long serialVersionUID = 1L;

    public HttpConfig() {
        this.headers = List.of(new KeyValueEnableParam());
    }

}
