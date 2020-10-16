package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.scenario.KeyValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JSONType(typeName = RequestType.SQL)
public class SqlRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.SQL;
    @JSONField(ordinal = 3)
    private String dataSource;
    @JSONField(ordinal = 4)
    private String query;
    @JSONField(ordinal = 5)
    private long queryTimeout;
    @JSONField(ordinal = 6)
    private Boolean useEnvironment;
    @JSONField(ordinal = 7)
    private Boolean followRedirects;
    @JSONField(ordinal = 13)
    private String resultVariable;
    @JSONField(ordinal = 14)
    private String variableNames;
    @JSONField(ordinal = 15)
    private List<KeyValue> variables;
}
