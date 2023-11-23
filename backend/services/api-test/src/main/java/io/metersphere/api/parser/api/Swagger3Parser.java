package io.metersphere.api.parser.api;

import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.parser.ImportParser;
import io.metersphere.sdk.util.LogUtils;

import java.io.InputStream;


public class Swagger3Parser<T> implements ImportParser<T> {

    @Override
    public T parse(InputStream source, ImportRequest request) throws Exception {
        LogUtils.info("Swagger3Parser parse");

        // todo: 检查swagger文件版本

        // todo：检查文件的合规性

        // todo：解析文件pojo
        return null;
    }
}
