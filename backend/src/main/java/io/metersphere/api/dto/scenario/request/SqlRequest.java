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
    @JSONField(ordinal = 50)
    private String dataSource;
    @JSONField(ordinal = 51)
    private String query;
    @JSONField(ordinal = 52)
    private long queryTimeout;
    @JSONField(ordinal = 53)
    private String resultVariable;
    @JSONField(ordinal = 54)
    private String variableNames;
    @JSONField(ordinal = 55)
    private List<KeyValue> variables;
}
