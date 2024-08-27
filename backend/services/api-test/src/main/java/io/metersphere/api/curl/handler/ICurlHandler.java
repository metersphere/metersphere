package io.metersphere.api.curl.handler;

import io.metersphere.api.curl.domain.CurlEntity;

/**
 * @author wx
 */
public interface ICurlHandler<R, S> {
 
    ICurlHandler<CurlEntity, String> next(ICurlHandler<CurlEntity, String> handler);
 
    void handle(CurlEntity entity, String curl);
}