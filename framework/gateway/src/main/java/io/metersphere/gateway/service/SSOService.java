package io.metersphere.gateway.service;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.domain.User;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.IOUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.request.LoginRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.WebSession;

import javax.annotation.Resource;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
@Transactional(rollbackFor = Exception.class)
public class SSOService {
    @Resource
    private AuthSourceService authSourceService;
    @Resource
    private Environment env;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserLoginService userLoginService;

    public void exchangeToken(String code, String authId, WebSession session, Locale locale) throws Exception {
        AuthSource authSource = authSourceService.getAuthSource(authId);
        Map config = JSON.parseObject(authSource.getConfiguration(), Map.class);
        String tokenUrl = (String) config.get("tokenUrl");

        RestTemplate restTemplate = getRestTemplateIgnoreSSL();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        //接口参数
        map.add("code", code);
        map.add("client_id", config.get("clientId"));
        map.add("client_secret", config.get("secret"));
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", config.get("redirectUrl"));
        //头部类型
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //构造实体对象
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, headers);
        //发起请求,服务地址，请求参数，返回消息体的数据类型
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, param, String.class);

        String content = response.getBody();
        Map jsonObject = JSON.parseObject(content, Map.class);
        String accessToken = (String) jsonObject.get("access_token");
        String idToken = (String) jsonObject.get("id_token");
        session.getAttributes().put("idToken", idToken);

        if (StringUtils.isBlank(accessToken)) {
            MSException.throwException(content);
        }

        doOICDLogin(authSource, accessToken, session, locale);
    }

    private RestTemplate getRestTemplateIgnoreSSL() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslsf)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        BasicHttpClientConnectionManager connectionManager =
                new BasicHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
                .setConnectionManager(connectionManager).build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
    }

    private void doOICDLogin(AuthSource authSource, String accessToken, WebSession session, Locale locale) throws Exception {
        Map config = JSON.parseObject(authSource.getConfiguration(), Map.class);
        String userInfoUrl = (String) config.get("userInfoUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        RestTemplate restTemplate = getRestTemplateIgnoreSSL();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, httpEntity, String.class);
        String content = response.getBody();
        Map jsonObject = JSON.parseObject(content, Map.class);
        String sub = StringUtils.substring((String) jsonObject.get("sub"), 0, 50);
        if (StringUtils.isBlank(sub)) {
            MSException.throwException(content);
        }
        String email = (String) jsonObject.get("email");
        String name = (String) jsonObject.get("name");
        // userId 或 email 有一个相同即为存在本地用户
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(sub);
        loginRequest.setPassword("nothing");
        loginRequest.setAuthenticate(authSource.getType());
        User u = userLoginService.selectUser(sub, email);
        if (u == null) {
            // 新建用户
            User user = new User();
            user.setId(sub);
            user.setName(name);
            user.setEmail(email);
            user.setSource(authSource.getType());
            userLoginService.createOssUser(user);
        } else {
            if (StringUtils.equals(u.getEmail(), email) && !StringUtils.equals(u.getId(), sub)) {
                MSException.throwException("email already exists!");
            }
        }
        Optional<SessionUser> userOptional = userLoginService.login(loginRequest, session, locale);
        session.getAttributes().put("authenticate", authSource.getType());
        session.getAttributes().put("authId", authSource.getId());
        session.getAttributes().put("user", userOptional.get());
    }

    /**
     * oidc logout
     */
//    public void logout() throws Exception {
//        String authId = (String) SecurityUtils.getSubject().getSession().getAttribute("authId");
//        AuthSource authSource = authSourceService.getAuthSource(authId);
//        if (authSource != null) {
//            Map config = JSON.parseObject(authSource.getConfiguration(), Map.class);
//            if (StringUtils.equals(UserSource.OIDC.name(), authSource.getType())) {
//                String idToken = (String) SecurityUtils.getSubject().getSession().getAttribute("idToken");
//                String logoutUrl = (String) config.get("logoutUrl");
//
//                RestTemplate restTemplate = getRestTemplateIgnoreSSL();
//                restTemplate.getForEntity(logoutUrl + "?id_token_hint=" + idToken, String.class);
//            }
//        }
//    }

    /**
     * cas callback
     */
    public void serviceValidate(String ticket, String authId, WebSession session, Locale locale) throws Exception {
        AuthSource authSource = authSourceService.getAuthSource(authId);
        Map config = JSON.parseObject(authSource.getConfiguration(), Map.class);
        String redirectUrl = ((String) config.get("redirectUrl")).replace("${authId}", authId);
        String validateUrl = (String) config.get("validateUrl");

        HttpClient httpclient = HttpClientBuilder.create().build();
        String serviceValidateUrl = validateUrl + "?service=" + redirectUrl + "&ticket=" + ticket;
        HttpGet httpGet = new HttpGet(serviceValidateUrl);
        HttpResponse response = httpclient.execute(httpGet);
        String body = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        String name = StringUtils.substringBetween(body, "<cas:user>", "</cas:user>");
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(name);
        loginRequest.setPassword("nothing");
        loginRequest.setAuthenticate(authSource.getType());
        User u = userLoginService.selectUser(name, name);
        if (u == null) {
            // 新建用户
            User user = new User();
            user.setId(name);
            user.setName(name);
            user.setEmail(StringUtils.contains(name, "@") ? name : null);
            user.setSource(authSource.getType());
            userLoginService.createOssUser(user);
        } else {
            if (StringUtils.equals(u.getEmail(), name) && !StringUtils.equals(u.getId(), name)) {
                MSException.throwException("email already exists!");
            }
        }
        Optional<SessionUser> userOptional = userLoginService.login(loginRequest, session, locale);
        session.getAttributes().put("authenticate", authSource.getType());
        session.getAttributes().put("authId", authSource.getId());
        session.getAttributes().put("user", userOptional.get());
        // 记录cas对应关系
        Long timeout = env.getProperty("spring.session.timeout", Long.class);
        stringRedisTemplate.opsForValue().set(ticket, name, timeout, TimeUnit.SECONDS);
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
