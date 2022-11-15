package io.metersphere.service;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.mapper.AuthSourceMapper;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
public class SSOLogoutService {
    @Resource
    private AuthSourceMapper authSourceMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisIndexedSessionRepository redisIndexedSessionRepository;
    /**
     * oidc logout
     */
    public void logout(String sessionId, HttpServletResponse response) throws Exception {
        Object obj = redisIndexedSessionRepository.findById(sessionId);
        String authId = (String) MethodUtils.invokeMethod(obj, "getAttribute", "authId");
        AuthSource authSource = authSourceMapper.selectByPrimaryKey(authId);
        if (authSource != null) {
            Map config = JSON.parseObject(authSource.getConfiguration(), Map.class);
            if (StringUtils.equals(UserSource.OIDC.name(), authSource.getType())) {
                String idToken = (String) MethodUtils.invokeMethod(obj, "getAttribute", "idToken");
                String logoutUrl = (String) config.get("logoutUrl");
                restTemplate.getForEntity(logoutUrl + "?id_token_hint=" + idToken, String.class);
            }
            if (StringUtils.equals(UserSource.CAS.name(), authSource.getType())) {
                String casTicket = (String) MethodUtils.invokeMethod(obj, "getAttribute", "casTicket");
                if (StringUtils.isNotEmpty(casTicket)) {
                    stringRedisTemplate.delete(casTicket);
                }
            }
            if (StringUtils.equals(UserSource.OAuth2.name(), authSource.getType())) {
                if (StringUtils.isNotBlank((String) config.get("logoutUrl"))) {
                    // 设置标志
                    response.setStatus(402);
                    response.setHeader("redirect", (String) config.get("logoutUrl"));
                }
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
