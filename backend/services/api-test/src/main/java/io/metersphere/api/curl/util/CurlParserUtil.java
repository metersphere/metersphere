package io.metersphere.api.curl.util;

import io.metersphere.api.curl.domain.CurlEntity;
import io.metersphere.api.curl.handler.*;

/**
 * @author wx
 */
public class CurlParserUtil {


    /**
     * 解析crul 工具类
     * @param curl
     * @return
     */
    public static CurlEntity parse(String curl) {
        CurlEntity entity = CurlEntity.builder().build();
        ICurlHandler<CurlEntity, String> handlerChain = CurlHandlerChain.init();

        handlerChain.next(new UrlPathHandler())
                .next(new QueryParamsHandler())
                .next(new HttpMethodHandler())
                .next(new HeaderHandler())
                .next(new HttpBodyHandler());
 
        handlerChain.handle(entity, curl);
        return entity;
    }
}