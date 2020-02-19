package io.metersphere.parse;

import io.metersphere.commons.constants.EngineType;
import io.metersphere.parse.xml.XmlEngineSourceParse;

public class EngineSourceParserFactory {
    public static EngineSourceParser createEngineSourceParser(String type) {
        final EngineType engineType = EngineType.valueOf(type);

        if (EngineType.JMX.equals(engineType)) {
            return new XmlEngineSourceParse();
        }

        return null;
    }
}
