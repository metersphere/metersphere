package io.metersphere.api.parse;

import io.metersphere.api.dto.parse.ApiImport;

import java.io.InputStream;

public interface ApiImportParser {
    ApiImport parse(InputStream source);
}
