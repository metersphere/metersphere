package io.metersphere.project.dto.environment.http;

import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.system.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class HttpConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "环境域名")
    private String url;
    /**
     *  启用条件
     * {@link HttpConfigMatchType}
     */
    @Schema(description = "启用条件  NONE/MODULE/PATH")
    @EnumValue(enumClass = HttpConfigMatchType.class)
    private String type = HttpConfigMatchType.NONE.name();
    @Valid
    @Schema(description = "路径匹配规则")
    private HttpConfigPathMatchRule pathMatchRule = new HttpConfigPathMatchRule();
    @Valid
    @Schema(description = "模块匹配规则")
    private HttpConfigModuleMatchRule moduleMatchRule = new HttpConfigModuleMatchRule();
    @Schema(description = "请求头")
    private List<@Valid KeyValueEnableParam> headers = new ArrayList<>(0);

    /**
     * 启用条件匹配类型
     */
    public enum HttpConfigMatchType {
        /**
         * 路径匹配
         */
        PATH,
        /**
         * 模块匹配
         */
        MODULE,
        /**
         * 无条件
         */
        NONE
    }
}
