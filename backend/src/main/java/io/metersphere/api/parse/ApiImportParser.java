package io.metersphere.api.parse;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;

import java.io.InputStream;

public interface ApiImportParser {
    ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request);
}
