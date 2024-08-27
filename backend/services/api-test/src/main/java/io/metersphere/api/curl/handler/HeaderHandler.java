package io.metersphere.api.curl.handler;

import io.metersphere.api.curl.constants.CurlPatternConstants;
import io.metersphere.api.curl.domain.CurlEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author wx
 */
public class HeaderHandler extends CurlHandlerChain {

    @Override
    public void handle(CurlEntity entity, String curl) {
        Map<String, String> headers = parseHeaders(curl);
        entity.setHeaders(headers);
        super.nextHandle(entity, curl);
    }

    /**
     * header解析
     *
     * @param curl
     * @return
     */
    private Map<String, String> parseHeaders(String curl) {
        if (StringUtils.isBlank(curl)) {
            return Collections.emptyMap();
        }

        Matcher matcher = CurlPatternConstants.CURL_HEADERS_PATTERN.matcher(curl);
        Map<String, String> headers = new HashMap<>();
        while (matcher.find()) {
            String header = "";
            if (matcher.group(1) != null) {
                header = matcher.group(1);
            } else {
                header = matcher.group(2);
            }
            String[] headerKeyValue = header.split(":", 2);
            if (headerKeyValue.length == 2) {
                // 去除键和值的首尾空白字符
                headers.put(headerKeyValue[0].trim(), headerKeyValue[1].trim());
            }
        }

        Matcher userMatcher = CurlPatternConstants.CURL_USER_HEAD_PATTERN.matcher(curl);
        if (userMatcher.find()) {
            String user = userMatcher.group(2);
            headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(user.getBytes()));
        }

        return headers;
    }

}