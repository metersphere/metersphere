package io.metersphere.api.parser;


import io.metersphere.api.dto.request.ImportRequest;

import java.io.InputStream;

public interface ImportParser<T> {
    T parse(InputStream source, ImportRequest request) throws Exception;
}
