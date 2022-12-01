package io.metersphere.commons.vo;

import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;

import java.util.List;

@Data
public class JDBCProcessorVO {
    private boolean enable;
    private String name;
    private String id;
    private String index;

    private DatabaseConfig dataSource;
    private String query;
    private long queryTimeout;
    private String resultVariable;
    private String variableNames;
    private List<KeyValue> variables;
    private String environmentId;
    private String dataSourceId;
    private String protocol = RequestTypeConstants.SQL;
    private String useEnvironment;
    private MsTestElement parent;

}
