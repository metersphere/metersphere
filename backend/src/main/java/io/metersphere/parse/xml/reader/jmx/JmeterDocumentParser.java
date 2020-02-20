package io.metersphere.parse.xml.reader.jmx;

import io.metersphere.engine.EngineContext;
import io.metersphere.parse.xml.reader.DocumentParser;
import org.junit.platform.commons.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class JmeterDocumentParser implements DocumentParser {
    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String STRING_PROP = "stringProp";
    private final static String CONCURRENCY_THREAD_GROUP = "com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup";
    private final static String VARIABLE_THROUGHPUT_TIMER = "kg.apc.jmeter.timers.VariableThroughputTimer";
    private EngineContext context;

    @Override
    public InputStream parse(EngineContext context, Document document) throws Exception {
        this.context = context;

        final Element jmeterTestPlan = document.getDocumentElement();

        NodeList childNodes = jmeterTestPlan.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node instanceof Element) {
                Element ele = (Element) node;

                // jmeterTestPlan的子元素肯定是<hashTree></hashTree>
                parseHashTree(ele);
            }
        }

        return documentToInputStream(document);
    }

    private InputStream documentToInputStream(Document document) throws TransformerException {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        final String resultStr = writer.toString();
        return new ByteArrayInputStream(resultStr.getBytes(StandardCharsets.UTF_8));
    }

    private void parseHashTree(Element hashTree) {
        if (invalid(hashTree)) {
            return;
        }

        if (hashTree.getChildNodes().getLength() > 0) {
            final NodeList childNodes = hashTree.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (invalid(ele)) {
                        continue;
                    }

                    if (nodeNameEquals(ele, HASH_TREE_ELEMENT)) {
                        parseHashTree(ele);
                    } else if (nodeNameEquals(ele, CONCURRENCY_THREAD_GROUP)) {
                        processConcurrencyThreadGroup(ele);
                    } else if (nodeNameEquals(ele, VARIABLE_THROUGHPUT_TIMER)) {
                        processVariableThroughputTimer(ele);
                    }
                }
            }
        }
    }

    private void processConcurrencyThreadGroup(Element concurrencyThreadGroup) {
        if (concurrencyThreadGroup.getChildNodes().getLength() > 0) {
            final NodeList childNodes = concurrencyThreadGroup.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (invalid(ele)) {
                        continue;
                    }

                    if (nodeNameEquals(ele, STRING_PROP)) {
                        parseStringProp(ele);
                    }
                }
            }
        }
    }

    private void processVariableThroughputTimer(Element variableThroughputTimer) {
        if (variableThroughputTimer.getChildNodes().getLength() > 0) {
            final NodeList childNodes = variableThroughputTimer.getChildNodes();
            int stringPropCount = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (invalid(ele)) {
                        continue;
                    }

                    // kg.apc.jmeter.timers.VariableThroughputTimer的stringProp的name属性是动态的
                    if (nodeNameEquals(ele, STRING_PROP)) {
                        if (stringPropCount < 2) {
                            stringPropCount++;
                        } else {
                            stringPropCount = 0;
                            ele.getFirstChild().setNodeValue(context.getProperty("duration").toString());
                            continue;
                        }
                        ele.getFirstChild().setNodeValue(context.getProperty("rpsLimit").toString());
                    }
                }
            }
        }
    }

    private void parseStringProp(Element stringProp) {
        if (stringProp.getChildNodes().getLength() > 0 && context.getProperty(stringProp.getAttribute("name")) != null) {
            stringProp.getFirstChild().setNodeValue(context.getProperty(stringProp.getAttribute("name")).toString());
        }
    }

    private boolean nodeNameEquals(Node node, String desiredName) {
        return desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName());
    }

    private boolean invalid(Element ele) {
        return !StringUtils.isBlank(ele.getAttribute("enabled")) && !Boolean.parseBoolean(ele.getAttribute("enabled"));
    }
}
