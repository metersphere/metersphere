package io.metersphere.performance.parse;

import io.metersphere.commons.constants.FileType;
import io.metersphere.performance.parse.xml.reader.JmeterDocumentParser;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EngineSourceParserFactory {
    public static final boolean IS_TRANS = true;


    public static EngineSourceParser createEngineSourceParser(String type) {
        final FileType engineType = FileType.valueOf(type);

        if (FileType.JMX.equals(engineType)) {
            return new JmeterDocumentParser();
        }

        return null;
    }

    public static Document getDocument(InputStream source) throws DocumentException {
        SAXReader reader = new SAXReader();
        if (!IS_TRANS) {
            reader.setXMLFilter(EngineSourceParserFactory.getFilter());
        }
        return reader.read(source);
    }

    public static byte[] getBytes(Document document) throws Exception {
        // todo 格式化代码会导致前后置脚本缩进有问题，先使用基本方式
        return document.asXML().getBytes(StandardCharsets.UTF_8);
    }

    public static XMLFilterImpl getFilter() {
        return new XMLFilterImpl() {
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                String text = new String(ch, start, length);

                if (length == 1) {
                    if (StringUtils.equals("&", text)) {
                        char[] escape = "&amp;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals("\"", text)) {
                        char[] escape = "&quot;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals("'", text)) {
                        char[] escape = "&apos;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals("<", text)) {
                        char[] escape = "&lt;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                    if (StringUtils.equals(">", text)) {
                        char[] escape = "&gt;".toCharArray();
                        super.characters(escape, start, escape.length);
                        return;
                    }
                }

                super.characters(ch, start, length);
            }
        };
    }
}
