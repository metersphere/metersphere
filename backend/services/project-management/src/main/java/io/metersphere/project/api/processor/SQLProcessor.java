package io.metersphere.project.api.processor;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.api.KeyValueParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 处理器
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
    @Size(max = 200)
    private String resultVariable;
    /**
     * 按列存储
     */
    @Size(max = 200)
    private String variableNames;
    /**
     * 数据源ID
     */
    @NotBlank
    @Size(max = 50)
    private String dataSourceId;
    /**
     * 数据源名称
     * 用于匹配不同环境数据源
     */
    @NotBlank
    @Size(max = 255)
    private String dataSourceName;
    /**
     * 提取参数
     */
    @Valid
    private List<KeyValueParam> extractParams = new ArrayList<>();
}
