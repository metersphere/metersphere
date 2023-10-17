package io.metersphere.system.dto;

import io.metersphere.system.domain.StatusDefinition;
import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StatusFlowSettingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<StatusItem> statusItems;
    private List<String> statusDefinitionTypes;
    private List<StatusFlow> statusFlows;
    private List<StatusDefinition> statusDefinitions;
}
