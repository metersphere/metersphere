package io.metersphere.api.parse.old;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.parse.ApiImport;

import java.io.InputStream;

public interface ApiImportParser {
    ApiImport parse(InputStream source, ApiTestImportRequest request);
    ApiDefinitionImport parseApi(InputStream source, ApiTestImportRequest request);

}
