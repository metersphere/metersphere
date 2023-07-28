package io.metersphere.api.dto.jmeter.sampler;

import io.metersphere.plugin.api.annotation.PluginSubType;
import io.metersphere.plugin.api.dto.TestElementDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
@Data
@EqualsAndHashCode(callSuper = true)
@PluginSubType("MSDebugSampler")
public class MSDebugSampler extends TestElementDTO {
    private boolean displayJMeterProperties = false;
    private boolean displayJMeterVariables = true;
    private boolean displaySystemProperties = false;


    private DebugSampler debugSampler() {
        DebugSampler debugSampler = new DebugSampler();
        debugSampler.setEnabled(this.isEnable());
        if (StringUtils.isEmpty(this.getName())) {
            this.setName(MSDebugSampler.class.getSimpleName());
        }
        debugSampler.setName(this.getName());
        debugSampler.setProperty(TestElement.TEST_CLASS, DebugSampler.class.getName());
        debugSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));

        debugSampler.setDisplaySystemProperties(this.displaySystemProperties);
        debugSampler.setDisplayJMeterVariables(this.displayJMeterVariables);
        debugSampler.setDisplayJMeterProperties(this.displayJMeterProperties);

        //上面三行直接Set属性会导致DebugSampler构建时取不到值，可能是JMeter的Bug,需要SetProperty
        debugSampler.setProperty("displayJMeterProperties", this.displayJMeterProperties);
        debugSampler.setProperty("displayJMeterVariables", this.displayJMeterVariables);
        debugSampler.setProperty("displaySystemProperties", this.displaySystemProperties);
        return debugSampler;
    }

}
