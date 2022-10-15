package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.shell.filter.ScriptFilter;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.Collection;
import java.util.List;

@Data
public class EnvJSR223Processor extends BaseEnvElement {
    private String type = ElementConstants.JSR223;
    private String clazzName = EnvJSR223Processor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;
}
