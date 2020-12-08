package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jorphan.collections.HashTree;

import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "scenario")
public class MsScenario extends MsTestElement {

    private String type = "scenario";
    @JSONField(ordinal = 10)
    private String name;

    @JSONField(ordinal = 11)
    private String referenced;

    @JSONField(ordinal = 12)
    private String environmentId;

    @JSONField(ordinal = 13)
    private List<KeyValue> variables;

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, EnvironmentConfig config) {
        if (environmentId != null) {
            ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
            ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
            config = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
        }
        if (this.getReferenced() != null && this.getReferenced().equals("Deleted")) {
            return;
        } else if (this.getReferenced() != null && this.getReferenced().equals("REF")) {
            try {
                ApiAutomationService apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiScenario scenario = apiAutomationService.getApiScenario(this.getId());
                JSONObject element = JSON.parseObject(scenario.getScenarioDefinition());
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                });
                if (hashTree == null) {
                    hashTree = elements;
                } else {
                    hashTree.addAll(elements);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                el.toHashTree(tree, el.getHashTree(), config);
            }
        }
    }
}
