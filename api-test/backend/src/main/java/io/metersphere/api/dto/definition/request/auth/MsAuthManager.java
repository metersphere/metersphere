package io.metersphere.api.dto.definition.request.auth;

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
@EqualsAndHashCode(callSuper = true)
public class MsAuthManager extends MsTestElement {
    private String type = ElementConstants.AUTH_MANAGER;
    private String clazzName = MsAuthManager.class.getCanonicalName();
    private String username;
    private String password;
    private String url;
    private String realm;
    private String verification;
    private String mechanism;
    private String encrypt;
    private String domain;
    private String environment;
    public static final Map<String, AuthManager.Mechanism> mechanismMap = new HashMap<>() {{
        this.put("Basic Auth", AuthManager.Mechanism.BASIC);
        this.put("Digest Auth", AuthManager.Mechanism.DIGEST);
    }};

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        if (!this.isEnable()) {
            return;
        }
        if (mechanismMap.containsKey(this.getVerification())) {
            ParameterConfig config = (ParameterConfig) msParameter;
            AuthManager authManager = initBase();
            Authorization auth = new Authorization();
            if (this.url != null) {
                auth.setURL(this.url);
            } else {
                if (config != null && config.isEffective(this.getProjectId())) {
                    if (config.isEffective(this.getProjectId())) {
                        String url = config.get(this.getProjectId()).getHttpConfig().getProtocol() + "://" + config.get(this.getProjectId()).getHttpConfig().getSocket();
                        auth.setURL(url);
                    }
                }
            }
            auth.setURL("");
            auth.setUser(this.username);
            auth.setPass(this.password);
            auth.setMechanism(mechanismMap.get(this.getVerification()));
            authManager.addAuth(auth);
            tree.add(authManager);
        }
    }

    public void setAuth(HashTree tree, MsAuthManager msAuthManager, HTTPSamplerProxy samplerProxy) {
        try {
            AuthManager authManager = initBase();
            Authorization auth = new Authorization();
            auth.setURL("");
            auth.setUser(msAuthManager.getUsername());
            auth.setPass(msAuthManager.getPassword());
            auth.setMechanism(mechanismMap.get(msAuthManager.getVerification()));
            authManager.addAuth(auth);
            tree.add(authManager);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private AuthManager initBase() {
        AuthManager authManager = new AuthManager();
        authManager.setEnabled(true);
        authManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : ElementConstants.AUTH_MANAGER);
        authManager.setProperty(TestElement.TEST_CLASS, AuthManager.class.getName());
        authManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("AuthPanel"));
        return authManager;
    }
}
