package io.metersphere.api.parse;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
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
import java.util.HashMap;
import java.util.Map;

public class JmeterDocumentParser {
    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String STRING_PROP = "stringProp";
    private final static String ARGUMENTS = "Arguments";
    private final static String COLLECTION_PROP = "collectionProp";
    private final static String HTTP_SAMPLER_PROXY = "MsHTTPSamplerProxy";
    private final static String ELEMENT_PROP = "elementProp";

    public static byte[] parse(byte[] source) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try (
                ByteArrayInputStream byteStream = new ByteArrayInputStream(source)
        ) {
            InputSource inputSource = new InputSource(byteStream);
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
            LogUtil.error(e.getMessage(), e);
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
                    } else if (nodeNameEquals(ele, ARGUMENTS)) {
                        processArguments(ele);
                    } else if (nodeNameEquals(ele, HTTP_SAMPLER_PROXY)) {
                        processHttpSamplerProxy(ele);
                    }
                }
            }
        }
    }

    private static void processHttpSamplerProxy(Element ele) {
        if (invalid(ele)) {
            return;
        }
        NodeList childNodes = ele.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (!(item instanceof Element)) {
                continue;
            }
            Element element = (Element) item;
            if (nodeNameEquals(element, ELEMENT_PROP) && "HTTPsampler.Arguments".equals(element.getAttribute("name"))) {
                processArguments(element);
            } else if ("HTTPSampler.path".equals(element.getAttribute("name"))) {
                processStringProp(element);
            }
        }
    }

    private static void processArguments(Element ele) {
        if (invalid(ele)) {
            return;
        }
        NodeList childNodes = ele.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (!(item instanceof Element)) {
                continue;
            }
            Element element = (Element) item;
            if (nodeNameEquals(item, COLLECTION_PROP) && "Arguments.arguments".equals(element.getAttribute("name"))) {
                NodeList elementProps = item.getChildNodes();
                for (int j = 0; j < elementProps.getLength(); j++) {
                    Node elementProp = elementProps.item(j);
                    if (!(elementProp instanceof Element)) {
                        continue;
                    }
                    NodeList stringProps = elementProp.getChildNodes();
                    for (int k = 0; k < stringProps.getLength(); k++) {
                        Node stringProp = stringProps.item(k);
                        if (!(stringProp instanceof Element)) {
                            continue;
                        }
                        processStringProp((Element) stringProp);
                    }
                }
            }
        }
    }

    private static void processStringProp(Element ele) {
        String name = ele.getAttribute("name");
        switch (name) {
            case "HTTPSampler.path":
                String path = ele.getTextContent();
                Map<String, String> parser = parserUrl(path);
                String url = parser.get("URL");
                String params = parser.keySet().stream().filter(k -> !"URL".equals(k)).reduce("?", (u, k) -> {
                    String v = parser.get(k);
                    if (!StringUtils.equals("?", u)) {
                        u += "&";
                    }
                    u += k + "=" + ScriptEngineUtils.buildFunctionCallString(v);
                    return u;
                });
                ele.setTextContent(url + ((params != null && !"?".equals(params)) ? params : ""));
                break;
            case "Argument.value":
                String textContent = ele.getTextContent();
                if (StringUtils.startsWith(textContent, "@")) {
                    ele.setTextContent(ScriptEngineUtils.buildFunctionCallString(textContent));
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

    private static Map<String, String> parserUrl(String url) {
//		传递的URL参数
        Map<String, String> strUrlParas = new HashMap<>();

        String strUrl;
        String strUrlParams;


//		解析访问地址
        if (url.contains("?")) {
            String[] strUrlPatten = url.split("\\?");
            strUrl = strUrlPatten[0];
            strUrlParams = strUrlPatten[1];

        } else {
            strUrl = url;
            strUrlParams = url;
        }

        strUrlParas.put("URL", strUrl);
//		解析参数
        String[] params = null;

        if (strUrlParams.contains("&")) {
            params = strUrlParams.split("&");
        } else {
            params = new String[]{strUrlParams};
        }

//		保存参数到参数容器
        for (String p : params) {
            if (p.contains("=")) {
                String[] param = p.split("=");
                if (param.length == 1) {
                    strUrlParas.put(param[0], "");
                } else {

                    String key = param[0];
                    String value = param[1];

                    strUrlParas.put(key, value);
                }
            }
        }
        return strUrlParas;
    }
}
