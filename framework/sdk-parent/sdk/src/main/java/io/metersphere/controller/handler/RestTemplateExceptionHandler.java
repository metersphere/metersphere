package io.metersphere.controller.handler;

import io.metersphere.commons.utils.JSON;
import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestTemplateExceptionHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        ResultHolder resultHolder = getResultHolder(response);
        if (resultHolder != null && !resultHolder.isSuccess()) {
            throw new RuntimeException(resultHolder.getMessage());
        }
        super.handleError(response);
    }


    private ResultHolder getResultHolder(ClientHttpResponse response) {
        try {
            return JSON.parseObject(IOUtils.toString(response.getBody(), StandardCharsets.UTF_8), ResultHolder.class);
        } catch (Exception ex) {
            // ignore
        }
        return null;
    }
}
