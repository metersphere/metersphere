package io.metersphere.system.security;

import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.domain.UserKey;
import io.metersphere.system.service.UserKeyService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class ApiKeyHandler {

    public static final String API_ACCESS_KEY = "accessKey";

    public static final String API_SIGNATURE = "signature";

    public static String getUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return getUser(request.getHeader(API_ACCESS_KEY), request.getHeader(API_SIGNATURE));
    }

    public static Boolean isApiKeyCall(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        return !StringUtils.isBlank(request.getHeader(API_ACCESS_KEY)) && !StringUtils.isBlank(request.getHeader(API_SIGNATURE));
    }

    public static String getUser(String accessKey, String signature) {
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(signature)) {
            return null;
        }
        UserKey userKey = CommonBeanFactory.getBean(UserKeyService.class).getUserKey(accessKey);
        if (userKey == null) {
            throw new RuntimeException("invalid accessKey");
        }
        if (BooleanUtils.isFalse(userKey.getEnable())) {
            throw new RuntimeException("accessKey is disabled");
        }
        if (BooleanUtils.isFalse(userKey.getForever())) {
            if (userKey.getExpireTime() == null || userKey.getExpireTime() < System.currentTimeMillis()) {
                throw new RuntimeException("accessKey is expired");
            }
        }
        String signatureDecrypt;
        try {
            signatureDecrypt = CodingUtils.aesDecrypt(signature, userKey.getSecretKey(), accessKey);
        } catch (Throwable t) {
            throw new RuntimeException("invalid signature");
        }
        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(signatureDecrypt), "|");
        if (signatureArray.length < 2) {
           return userKey.getCreateUser(); // TODO 兼容直接输入 AK/SK 并未拼接情况
        }
        if (!StringUtils.equals(accessKey, signatureArray[0])) {
            throw new RuntimeException("invalid signature");
        }
        long signatureTime;
        try {
            signatureTime = Long.parseLong(signatureArray[signatureArray.length - 1]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (Math.abs(System.currentTimeMillis() - signatureTime) > 1800000) {
            //签名30分钟超时
            throw new RuntimeException("expired signature");
        }
        return userKey.getCreateUser();
    }
}
