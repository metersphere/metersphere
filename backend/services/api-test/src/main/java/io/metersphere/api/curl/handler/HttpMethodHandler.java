package io.metersphere.api.curl.handler;

import io.metersphere.api.curl.constants.CurlPatternConstants;
import io.metersphere.api.curl.domain.CurlEntity;

import java.util.regex.Matcher;

/**
 * @author wx
 */
public class HttpMethodHandler extends CurlHandlerChain {

    @Override
    public void handle(CurlEntity entity, String curl) {
        CurlEntity.Method method = parseMethod(curl);
        entity.setMethod(method);
        super.nextHandle(entity, curl);
    }

    /**
     * 请求方法解析
     *
     * @param curl
     * @return
     */
    private CurlEntity.Method parseMethod(String curl) {
        Matcher matcher = CurlPatternConstants.HTTP_METHOD_PATTERN.matcher(curl);
        Matcher defaultMatcher = CurlPatternConstants.DEFAULT_HTTP_METHOD_PATTERN.matcher(curl);
        Matcher headMatcher = CurlPatternConstants.HTTP_HEAD_METHOD_PATTERN.matcher(curl);
        if (matcher.find()) {
            String method = matcher.group(2);
            return CurlEntity.Method.valueOf(method.toUpperCase());
        } else if (headMatcher.find()) {
            return CurlEntity.Method.HEAD;
        } else if (defaultMatcher.find()) {
            //如果命令中包含 -d 或 --data，没有明确请求方法，默认为 POST
            return CurlEntity.Method.POST;
        } else {
            //没有明确指定请求方法，默认为 GET
            return CurlEntity.Method.GET;
        }
    }

}