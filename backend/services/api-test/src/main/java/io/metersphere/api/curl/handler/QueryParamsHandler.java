package io.metersphere.api.curl.handler;

import io.metersphere.api.curl.constants.CurlPatternConstants;
import io.metersphere.api.curl.domain.CurlEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author wx
 */
public class QueryParamsHandler extends CurlHandlerChain {

    @Override
    public void handle(CurlEntity entity, String curl) {
        String url = extractUrl(curl);
        Map<String, String> queryParams = parseQueryParams(url);
        entity.setQueryParams(queryParams);
        super.nextHandle(entity, curl);
    }

    private String extractUrl(String curl) {
        Matcher matcher = CurlPatternConstants.URL_PARAMS_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * query参数解析
     *
     * @param url
     * @return
     */
    private Map<String, String> parseQueryParams(String url) {
        if (StringUtils.isBlank(url)) {
            return Collections.emptyMap();
        }

        Map<String, String> queryParams = new HashMap<>();
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1) {
            String query = urlParts[1];
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && idx < pair.length() - 1) {
                    String key = pair.substring(0, idx);
                    String value = pair.substring(idx + 1);
                    queryParams.put(key, value);
                } else {
                    queryParams.put(pair, null);
                }
            }
        }
        return queryParams;
    }

}