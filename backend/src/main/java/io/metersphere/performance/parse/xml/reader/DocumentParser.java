package io.metersphere.performance.parse.xml.reader;

import io.metersphere.performance.engine.EngineContext;
import org.w3c.dom.Document;

public interface DocumentParser {
    String parse(EngineContext context, Document document) throws Exception;
}
