package io.metersphere.api.dto.request.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.api.dto.request.http.KeyValueParam;
import lombok.Data;

import java.util.List;

/**
 * 公共脚本处理器
 * @Author: jianxing
 * @CreateTime: 2023-11-07  09:59
 */
@Data
@JsonTypeName("COMMON_SCRIPT")
public class CommonScriptProcessor extends MsProcessor {
    /**
     * 脚本ID
     */
    private String scriptId;
    /**
     * 入参
     */
    private List<KeyValueParam> params;
}
