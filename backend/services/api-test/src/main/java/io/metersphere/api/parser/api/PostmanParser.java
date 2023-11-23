package io.metersphere.api.parser.api;


import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.parser.ImportParser;
import io.metersphere.sdk.util.LogUtils;

import java.io.InputStream;

public class PostmanParser<T> implements ImportParser<T> {

    @Override
    public T parse(InputStream source, ImportRequest request) {
        LogUtils.info("PostmanParser parse");
        return null;
    }
}
