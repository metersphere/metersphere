package io.metersphere.parse;

import io.metersphere.engine.EngineContext;

import java.io.InputStream;

public interface EngineSourceParser {
    byte[] parse(EngineContext context, InputStream source) throws Exception;
}
