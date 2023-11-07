package io.metersphere.sdk.dto.api.request.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.sdk.dto.api.request.http.KeyValueParam;
import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  21:12
 */
@Data
@JsonTypeName("SQL")
public class SQLProcessor extends MsProcessor {
    /**
     * 脚本内容
     */
    private String script;
    /**
     * 超时时间
     */
    private long queryTimeout;
    /**
     * 存储结果
     */
    private String resultVariable;
    /**
     * 按列存储
     */
    private String variableNames;
    /**
     * 变量列表
     */
    private List<KeyValueParam> variables;
    /**
     * 环境ID
     */
    private String environmentId;
    /**
     * 数据源ID
     */
    private String dataSourceId;
    /**
     * 是否启用
     */
    private Boolean enable;
}
