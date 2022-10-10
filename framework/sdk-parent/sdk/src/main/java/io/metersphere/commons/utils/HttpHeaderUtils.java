package io.metersphere.commons.utils;

import io.metersphere.base.domain.UserKey;
import io.metersphere.commons.constants.ApiKeyConstants;
import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.UserKeyService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 服务之间调用，需要添加HttpHeader,获取的时候注意当前线程的位置
 */
public class HttpHeaderUtils {

    private static final ThreadLocal<UserDTO> sessionUserThreadLocal = new ThreadLocal<>();

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.add(HttpHeaders.COOKIE, SessionUtils.getHttpHeader(HttpHeaders.COOKIE));

        if (StringUtils.isNotBlank(SessionUtils.getHttpHeader(SessionConstants.HEADER_TOKEN))) {
            headers.add(SessionConstants.HEADER_TOKEN, SessionUtils.getHttpHeader(SessionConstants.HEADER_TOKEN));
        }
        if (StringUtils.isNotBlank(SessionUtils.getHttpHeader(SessionConstants.CSRF_TOKEN))) {
            headers.add(SessionConstants.CSRF_TOKEN, SessionUtils.getHttpHeader(SessionConstants.CSRF_TOKEN));
        }
        if (StringUtils.isNotBlank(SessionUtils.getHttpHeader(SessionConstants.CURRENT_PROJECT))) {
            headers.add(SessionConstants.CURRENT_PROJECT, SessionUtils.getHttpHeader(SessionConstants.CURRENT_PROJECT));
        }
        if (StringUtils.isNotBlank(SessionUtils.getHttpHeader(SessionConstants.CURRENT_WORKSPACE))) {
            headers.add(SessionConstants.CURRENT_WORKSPACE, SessionUtils.getHttpHeader(SessionConstants.CURRENT_WORKSPACE));
        }

        UserDTO user = sessionUserThreadLocal.get();
        if (user != null) {
            UserKey userKey = getUserKey(user);
            String accessKey = userKey.getAccessKey();
            String secretKey = userKey.getSecretKey();
            headers.add(SessionConstants.ACCESS_KEY, accessKey);
            headers.add(SessionConstants.SIGNATURE, CodingUtil.aesDecrypt(accessKey + "|" + System.currentTimeMillis(), secretKey, accessKey));
            headers.remove(HttpHeaders.COOKIE);
            sessionUserThreadLocal.remove();
        }

        return headers;
    }

    private static UserKey getUserKey(UserDTO user) {
        UserKeyService userKeyService = CommonBeanFactory.getBean(UserKeyService.class);
        List<UserKey> userKeys = userKeyService.getUserKeysInfo(user.getId());
        UserKey userKey;
        if (CollectionUtils.isEmpty(userKeys)) {
            userKey = userKeyService.generateUserKey(user.getId());
        } else {
            Optional<UserKey> ukOp = userKeys.stream().filter(uk -> StringUtils.equals(uk.getStatus(), ApiKeyConstants.ACTIVE.name())).findAny();
            if (ukOp.isEmpty()) {
                MSException.throwException("用户[" + user.getId() + "]至少需要开启一个ApiKey");
            }
            userKey = ukOp.get();
        }
        return userKey;
    }

    public static void runAsUser(UserDTO user) {
        if (user != null) {
            if (StringUtils.isBlank(user.getId())) {
                throw new IllegalArgumentException("User ID can't be null or empty.");
            }
            sessionUserThreadLocal.set(user);
        } else {
            sessionUserThreadLocal.remove();
        }
    }
}
