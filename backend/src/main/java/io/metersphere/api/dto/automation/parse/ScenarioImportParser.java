package io.metersphere.api.dto.automation.parse;

import io.metersphere.api.dto.ApiTestImportRequest;

import java.io.InputStream;

public interface ScenarioImportParser {
    ScenarioImport parse(InputStream source, ApiTestImportRequest request);
}
