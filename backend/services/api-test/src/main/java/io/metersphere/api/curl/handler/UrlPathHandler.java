package io.metersphere.api.curl.handler;

import io.metersphere.api.curl.constants.CurlPatternConstants;
import io.metersphere.api.curl.domain.CurlEntity;

import java.util.regex.Matcher;

/**
 * @author wx
 */
public class UrlPathHandler extends CurlHandlerChain {

    @Override
    public void handle(CurlEntity entity, String curl) {
        String url = parseUrlPath(curl);
        entity.setUrl(url);
        super.nextHandle(entity, curl);
    }

    /**
     * url路径解析
     *
     * @param curl
     * @return
     */
    private String parseUrlPath(String curl) {
        Matcher matcher = CurlPatternConstants.URL_PATH_PATTERN.matcher(curl);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
        }
        return null;
    }

}