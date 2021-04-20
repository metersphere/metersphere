package io.metersphere.api.dto.scenario;

import io.metersphere.commons.constants.ConditionType;
import io.metersphere.commons.utils.BeanUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class HttpConfig {
    private String socket;
    private String domain;
    private String protocol = "https";
    private String defaultCondition;
    private int port;
    private boolean isMock;
    private List<HttpConfigCondition> conditions;
    private List<KeyValue> headers;


    public boolean isNode(String type) {
        return StringUtils.equals(defaultCondition, type);
    }

    public HttpConfig initHttpConfig(HttpConfigCondition configCondition) {
        HttpConfig config = new HttpConfig();
        BeanUtils.copyBean(config, configCondition);
        return config;
    }

    public HttpConfig getPathCondition(String path) {
        List<HttpConfigCondition> conditions = this.getConditions().stream().filter(condition -> ConditionType.PATH.name().equals(condition.getType()) && CollectionUtils.isNotEmpty(condition.getDetails())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(conditions)) {
            for (HttpConfigCondition item : conditions) {
                List<KeyValue> details = item.getDetails().stream().filter(detail -> (detail.getValue().equals("contains") && StringUtils.contains(path, detail.getName())) || (detail.getValue().equals("equals") && StringUtils.equals(path, detail.getName()))).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(details)) {
                    return initHttpConfig(item);
                }
            }
        }
        return null;
    }

    public HttpConfig getModuleCondition(String moduleId) {
        List<HttpConfigCondition> conditions = this.getConditions().stream().filter(condition -> ConditionType.MODULE.name().equals(condition.getType()) && CollectionUtils.isNotEmpty(condition.getDetails())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(conditions)) {
            for (HttpConfigCondition item : conditions) {
                List<KeyValue> details = item.getDetails().stream().filter(detail -> StringUtils.contains(detail.getValue(), moduleId)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(details)) {
                    return initHttpConfig(item);
                }
            }
        }
        return null;
    }
}
