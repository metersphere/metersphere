package io.metersphere.parse.xml.reader;

import io.metersphere.commons.constants.EngineType;
import io.metersphere.parse.xml.reader.jmx.JmeterDocumentParser;

public class DocumentParserFactory {
    public static DocumentParser createDocumentParser(String type) {
        final EngineType engineType = EngineType.valueOf(type);

        if (EngineType.JMX.equals(engineType)) {
            return new JmeterDocumentParser();
        }

        return null;
    }
}
