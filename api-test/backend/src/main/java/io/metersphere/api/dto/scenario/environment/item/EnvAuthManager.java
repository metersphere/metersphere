package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class EnvAuthManager extends BaseEnvElement {
    private String type = ElementConstants.AUTH_MANAGER;
    private String clazzName = EnvAuthManager.class.getCanonicalName();
    private String username;
    private String password;
    private String url;
    private String realm;
    private String verification;
    private String mechanism;
    private String encrypt;
    private String domain;
    private String environment;
}
