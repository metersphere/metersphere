package io.metersphere.performance.parse;

import io.metersphere.performance.engine.EngineContext;

import java.io.InputStream;

public interface EngineSourceParser {
    String parse(EngineContext context, InputStream source) throws Exception;
}
