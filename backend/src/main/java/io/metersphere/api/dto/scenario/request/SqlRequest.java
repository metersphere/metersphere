package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.scenario.assertions.Assertions;
import io.metersphere.api.dto.scenario.extract.Extract;
import io.metersphere.api.dto.scenario.processor.JSR223PostProcessor;
import io.metersphere.api.dto.scenario.processor.JSR223PreProcessor;
import lombok.Data;

@Data
@JSONType(typeName = RequestType.SQL)
public class SqlRequest implements Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.SQL;
    @JSONField(ordinal = 1)
    private String id;
    @JSONField(ordinal = 2)
    private String name;
    @JSONField(ordinal = 3)
    private String dataSource;
    @JSONField(ordinal = 4)
    private String query;
    @JSONField(ordinal = 5)
    private long queryTimeout;
    @JSONField(ordinal = 6)
    private Boolean useEnvironment;
    @JSONField(ordinal = 7)
    private Assertions assertions;
    @JSONField(ordinal = 8)
    private Extract extract;
    @JSONField(ordinal = 9)
    private Boolean enable;
    @JSONField(ordinal = 10)
    private Boolean followRedirects;
    @JSONField(ordinal = 11)
    private JSR223PreProcessor jsr223PreProcessor;
    @JSONField(ordinal = 12)
    private JSR223PostProcessor jsr223PostProcessor;
}
