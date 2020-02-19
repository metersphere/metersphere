package io.metersphere.parse.xml.reader;

import io.metersphere.engine.EngineContext;
import org.w3c.dom.Document;

import java.io.InputStream;

public interface DocumentParser {
    InputStream parse(EngineContext context, Document document) throws Exception;
}
