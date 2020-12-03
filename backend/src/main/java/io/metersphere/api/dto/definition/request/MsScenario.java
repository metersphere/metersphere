package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.gson.Gson;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.commons.utils.CommonBeanFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jorphan.collections.HashTree;

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

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree) {
        if (this.getReferenced() != null && this.getReferenced().equals("Deleted")) {
            return;
        } else if (this.getReferenced() != null && this.getReferenced().equals("REF")) {
            ApiAutomationService apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
            ApiScenario scenario = apiAutomationService.getApiScenario(this.getId());
            Gson gs = new Gson();
            MsTestElement element = gs.fromJson(scenario.getScenarioDefinition(), MsTestElement.class);
            hashTree.add(element);
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(tree, el.getHashTree());
            });
        }
    }
}
