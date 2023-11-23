package io.metersphere.api.parser;

import io.metersphere.api.dto.request.ExportRequest;

public interface ExportParser<T> {
    T parse(ExportRequest request) throws Exception;
}
