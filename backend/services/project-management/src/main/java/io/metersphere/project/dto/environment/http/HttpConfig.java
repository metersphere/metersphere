package io.metersphere.project.dto.environment.http;

import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.dto.environment.auth.HTTPAuthConfig;
import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class HttpConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    private String id;
    @Schema(description = "http协议类型(http/https)")
    @EnumValue(enumClass = HttpProtocolType.class)
    private String protocol = HttpProtocolType.HTTP.name();
    @Schema(description = "环境域名")
    private String hostname;
    @Schema(description = "完整url")
    private String url;
    /**
     * 启用条件
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
    @Schema(description = "描述")
    private String description;
    @Schema(description = "排序")
    private int order;

    @Schema(description = "认证配置")
    private HTTPAuthConfig authConfig = new HTTPAuthConfig();


    public boolean isModuleMatchRule() {
        return StringUtils.equals(HttpConfigMatchType.MODULE.name(), type);
    }

    public boolean isPathMatchRule() {
        return StringUtils.equals(HttpConfigMatchType.PATH.name(), type);
    }

    public int getModuleMatchRuleOrder() {
        if (isPathMatchRule()) {
            return 0;
        } else if (isModuleMatchRule()) {
            return 1;
        }
        return 2;
    }

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

    /**
     * 启用条件匹配类型
     */
    public enum HttpProtocolType implements ValueEnum {
        HTTP("http"),
        HTTPS("https");

        private String value;

        HttpProtocolType(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return this.value;
        }
    }
}
