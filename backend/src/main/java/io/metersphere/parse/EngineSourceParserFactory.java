package io.metersphere.parse;

import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.parse.xml.XmlEngineSourceParse;

public class EngineSourceParserFactory {
    public static EngineSourceParser createEngineSourceParser(String type) {
        final FileType engineType = FileType.valueOf(type);

        if (FileType.JMX.equals(engineType)) {
            return CommonBeanFactory.getBean(XmlEngineSourceParse.class);
        }

        return null;
    }
}
