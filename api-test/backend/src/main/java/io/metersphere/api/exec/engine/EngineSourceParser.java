package io.metersphere.api.exec.engine;

import io.metersphere.engine.EngineContext;

import java.io.InputStream;

public interface EngineSourceParser {
    byte[] parse(EngineContext context, InputStream source) throws Exception;
}
