package io.metersphere.security;

import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.UUID;

public class SSOSessionHandler {

    public static String random = UUID.randomUUID() + UUID.randomUUID().toString();

    public static String validate(HttpServletRequest request) {
        try {
            String v = request.getHeader(SessionConstants.CSRF_TOKEN);
            if (StringUtils.isNotBlank(v)) {
                return validate(v);
            }
        } catch (Exception ignore) {
        }

        return null;
    }

    private static String validate(String csrfToken) {
        csrfToken = CodingUtil.aesDecrypt(csrfToken, SessionUser.secret, SessionUser.iv);
        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(csrfToken), "|");
        if (signatureArray.length != 4) {
            LogUtil.error("invalid token: {}, csrfToken: {}", signatureArray, csrfToken);
            throw new RuntimeException("invalid token");
        }
        return signatureArray[0];
    }
}
