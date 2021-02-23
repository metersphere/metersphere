package io.metersphere.api.parse;

import io.metersphere.api.dto.ApiTestImportRequest;

import java.io.InputStream;

public interface ApiImportParser<T> {
    T parse(InputStream source, ApiTestImportRequest request);
}
