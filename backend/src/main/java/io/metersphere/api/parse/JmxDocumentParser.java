package io.metersphere.api.parse;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ScriptEngineUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

public class JmxDocumentParser {
    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String STRING_PROP = "stringProp";

    public static byte[] parse(byte[] source) {
        final InputSource inputSource = new InputSource(new ByteArrayInputStream(source));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            final Document document = docBuilder.parse(inputSource);
            final Element jmeterTestPlan = document.getDocumentElement();

            NodeList childNodes = jmeterTestPlan.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    parseHashTree(ele);
                }
            }
            return documentToBytes(document);
        } catch (Exception e) {
            LogUtil.error(e);
            return source;
        }
    }

    private static byte[] documentToBytes(Document document) throws TransformerException {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString().getBytes();
    }

    private static void parseHashTree(Element hashTree) {
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
                    } else {
                        if (nodeNameEquals(ele, STRING_PROP)) {
                            processStringProp(ele);
                        }
                    }
                }
            }
        }
    }

    private static void processStringProp(Element ele) {
        String name = ele.getAttribute("name");
        NodeList childNodes;
        switch (name) {
            case "HTTPSampler.path":
                childNodes = ele.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node instanceof Element) {
                        String nodeValue = node.getNodeValue();
                        System.out.println(nodeValue);
                    }
                }
                break;
            case "Argument.value":
                childNodes = ele.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node instanceof Element) {
                        String nodeValue = node.getNodeValue();
                        node.setNodeValue(ScriptEngineUtils.calculate(nodeValue));
                    }
                }
                break;
            default:
                break;
        }
    }

    private static boolean nodeNameEquals(Node node, String desiredName) {
        return desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName());
    }

    private static boolean invalid(Element ele) {
        return !StringUtils.isBlank(ele.getAttribute("enabled")) && !Boolean.parseBoolean(ele.getAttribute("enabled"));
    }

}
