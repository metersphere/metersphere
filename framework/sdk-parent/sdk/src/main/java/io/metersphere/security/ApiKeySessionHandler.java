package io.metersphere.security;

import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ApiKeySessionHandler {

    public static final String SSO_SOURCE_ID = "sourceId";

    public static String random = UUID.randomUUID() + UUID.randomUUID().toString();

    public static String validate(HttpServletRequest request) {
        try {
            String v = request.getHeader(SessionConstants.CSRF_TOKEN);
            if (StringUtils.isNotBlank(v)) {
                return validate(v);
            }
        } catch (Exception e) {
            LogUtil.error("failed to validate", e);
        }

        return null;
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response, String... remainSessionId) {
        try {
            Set<String> remainSessionIdSet = new HashSet<>();
            int len$;
            int i$;
            if (remainSessionId != null && remainSessionId.length > 0) {
                String[] arr$ = remainSessionId;
                len$ = remainSessionId.length;

                for (i$ = 0; i$ < len$; ++i$) {
                    String s = arr$[i$];
                    if (s != null && !StringUtils.EMPTY.equals(s)) {
                        remainSessionIdSet.add(s.toLowerCase());
                    }
                }
            }

            if (request.getCookies() != null) {
                Cookie[] arr$ = request.getCookies();
                len$ = arr$.length;

                for (i$ = 0; i$ < len$; ++i$) {
                    Cookie cookie = arr$[i$];
                    if (!cookie.getName().toLowerCase().contains("rememberme") && (remainSessionIdSet.size() == 0 || !remainSessionIdSet.contains(cookie.getName().toLowerCase()))) {
                        cookie.setValue("deleteMe");
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            } else {
                Cookie cookie = new Cookie("MS_SESSION_ID", "deleteMe");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
            request.logout();
        } catch (Exception var8) {
            LogUtil.error("failed to logout", var8);
        }

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
