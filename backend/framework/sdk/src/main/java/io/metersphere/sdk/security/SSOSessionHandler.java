package io.metersphere.sdk.security;

import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.util.CodingUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class SSOSessionHandler {

    public static String random = UUID.randomUUID() + UUID.randomUUID().toString();

    public static String validate(HttpServletRequest request) {
        try {
            String v = request.getHeader(SessionConstants.CSRF_TOKEN);
            if (StringUtils.isNotBlank(v)) {
                return validate(v);
            }
        } catch (Exception e) {
//            LogUtil.error("failed to validate", e);
        }

        return null;
    }

    private static String validate(String csrfToken) {
        csrfToken = CodingUtil.aesDecrypt(csrfToken, SessionUser.secret, SessionUser.iv);
        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(csrfToken), "|");
        if (signatureArray.length != 4) {
            throw new RuntimeException("invalid token");
        }
        return signatureArray[0];
    }
}
