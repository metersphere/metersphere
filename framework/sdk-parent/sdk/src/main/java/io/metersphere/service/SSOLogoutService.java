package io.metersphere.service;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.mapper.AuthSourceMapper;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    public void kickOutUser(String logoutToken) {
        String[] split = StringUtils.split(logoutToken, '.');
        for (String s : split) {
            String v = CodingUtil.base64Decoding(s);
            Map obj = JSON.parseMap(v);
            String sub = (String) obj.get("sub");
            if (StringUtils.isNotEmpty(sub)) {
                SessionUtils.kickOutUser(sub);
                break;
            }
        }
    }

    public void kickOutCasUser(String logoutRequest) {
        String ticket = StringUtils.substringBetween(logoutRequest, "<samlp:SessionIndex>", "</samlp:SessionIndex>");
        if (StringUtils.isEmpty(ticket)) {
            return;
        }
        String name = stringRedisTemplate.opsForValue().get(ticket);
        if (StringUtils.isEmpty(name)) {
            return;
        }

        SessionUtils.kickOutUser(name);
        stringRedisTemplate.delete(ticket);
    }
}
