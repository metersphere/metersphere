package io.metersphere.project.dto.environment.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.dto.environment.KeyValueEnableParam;
import io.metersphere.project.dto.environment.KeyValueParam;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("ENV_SQL")
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
    private List<KeyValueEnableParam> variables;
    /**
     * 环境ID
     */
    private String environmentId;
    /**
     * 数据源ID
     */
    private String dataSourceId;
    /**
     * 提取参数
     */
    private List<KeyValueParam> extractParams;
}
