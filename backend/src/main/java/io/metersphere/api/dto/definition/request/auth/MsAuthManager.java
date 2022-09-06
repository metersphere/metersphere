package io.metersphere.api.dto.definition.request.auth;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.ParameterConfig;
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
@JSONType(typeName = "AuthManager")
public class MsAuthManager extends MsTestElement {
    private String type = "AuthManager";
    private String clazzName = MsAuthManager.class.getCanonicalName();

    @JSONField(ordinal = 20)
    private String username;

    @JSONField(ordinal = 21)
    private String password;

    @JSONField(ordinal = 22)
    private String url;

    @JSONField(ordinal = 23)
    private String realm;

    @JSONField(ordinal = 24)
    private String verification;

    @JSONField(ordinal = 25)
    private String mechanism;

    @JSONField(ordinal = 26)
    private String encrypt;

    @JSONField(ordinal = 27)
    private String domain;

    @JSONField(ordinal = 28)
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
        authManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : "AuthManager");
        authManager.setProperty(TestElement.TEST_CLASS, AuthManager.class.getName());
        authManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("AuthPanel"));
        return authManager;
    }
}
