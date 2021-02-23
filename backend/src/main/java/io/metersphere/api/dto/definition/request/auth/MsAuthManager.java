package io.metersphere.api.dto.definition.request.auth;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "AuthManager")
public class MsAuthManager extends MsTestElement {
    private String type = "AuthManager";
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

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (!this.isEnable()) {
            return;
        }
        AuthManager authManager = new AuthManager();
        authManager.setEnabled(true);
        authManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : "AuthManager");
        authManager.setProperty(TestElement.TEST_CLASS, AuthManager.class.getName());
        authManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("AuthPanel"));
        Authorization auth = new Authorization();
        if (this.url != null) {
            auth.setURL(this.url);
        } else {
            if (environment != null) {
                ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                ApiTestEnvironmentWithBLOBs environmentWithBLOBs = environmentService.get(environment);
                EnvironmentConfig envConfig = JSONObject.parseObject(environmentWithBLOBs.getConfig(), EnvironmentConfig.class);
                this.url = envConfig.getHttpConfig().getProtocol() + "://" + envConfig.getHttpConfig().getSocket();
            }
        }
        auth.setDomain(this.domain);
        auth.setUser(this.username);
        auth.setPass(this.password);
        auth.setMechanism(AuthManager.Mechanism.DIGEST);
        authManager.addAuth(auth);
        tree.add(authManager);
    }
}
