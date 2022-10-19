package io.metersphere.service;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.mapper.AuthSourceMapper;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.utils.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class SSOLogoutService {
    @Resource
    private AuthSourceMapper authSourceMapper;
    @Resource
    private RestTemplate restTemplate;

    /**
     * oidc logout
     */
    public void logout() throws Exception {
        String authId = (String) SecurityUtils.getSubject().getSession().getAttribute("authId");
        AuthSource authSource = authSourceMapper.selectByPrimaryKey(authId);
        if (authSource != null) {
            Map config = JSON.parseObject(authSource.getConfiguration(), Map.class);
            if (StringUtils.equals(UserSource.OIDC.name(), authSource.getType())) {
                String idToken = (String) SecurityUtils.getSubject().getSession().getAttribute("idToken");
                String logoutUrl = (String) config.get("logoutUrl");

                restTemplate.getForEntity(logoutUrl + "?id_token_hint=" + idToken, String.class);
            }
        }
    }
}
