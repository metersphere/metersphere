package io.metersphere;

import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

public class JmxFileParseTest {
    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String STRING_PROP = "stringProp";
    private final static String CONCURRENCY_THREAD_GROUP = "com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup";
    private final static String VARIABLE_THROUGHPUT_TIMER = "kg.apc.jmeter.timers.VariableThroughputTimer";
    private final static String THREAD_GROUP = "ThreadGroup";

    @Test
    public void parse() throws Exception {
        File file = new File("/Users/liuruibin/Downloads/blaze_meter_dev2.jmx");

        final FileInputStream inputStream = new FileInputStream(file);
        final InputSource inputSource = new InputSource(inputStream);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        final Document document = docBuilder.parse(inputSource);

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


        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        System.out.println("XML IN String format is: \n" + writer.toString());
    }

    private void parseHashTree(Element hashTree) {
        if (!valid(hashTree)) {
            return;
        }

        if (hashTree.getChildNodes().getLength() > 0) {
            final NodeList childNodes = hashTree.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (!valid(ele)) {
                        continue;
                    }

                    if (nodeNameEquals(ele, HASH_TREE_ELEMENT)) {
                        parseHashTree(ele);
                    } else if (nodeNameEquals(ele, CONCURRENCY_THREAD_GROUP)) {
                        processConcurrencyThreadGroup(ele);
                        processTimer(ele);
                    } else if (nodeNameEquals(ele, THREAD_GROUP)) {
                        processThreadGroup(ele);
                        processConcurrencyThreadGroup(ele);
                        processTimer(ele);
                    } else if (nodeNameEquals(ele, VARIABLE_THROUGHPUT_TIMER)) {

                    }
                }
            }
        }
    }

    private void processThreadGroup(Element threadGroup) {
        // 重命名 tagName
        Document document = threadGroup.getOwnerDocument();
        document.renameNode(threadGroup, threadGroup.getNamespaceURI(), CONCURRENCY_THREAD_GROUP);
        threadGroup.setAttribute("guiclass", CONCURRENCY_THREAD_GROUP + "Gui");
        threadGroup.setAttribute("testclass", CONCURRENCY_THREAD_GROUP);
        /*
        <elementProp name="ThreadGroup.main_controller" elementType="com.blazemeter.jmeter.control.VirtualUserController"/>
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <stringProp name="TargetLevel">2</stringProp>
        <stringProp name="RampUp">12</stringProp>
        <stringProp name="Steps">2</stringProp>
        <stringProp name="Hold">3</stringProp>
        <stringProp name="LogFilename"></stringProp>
        <stringProp name="Iterations">1</stringProp>
        <stringProp name="Unit">S</stringProp>
         */
        removeChildren(threadGroup);
        // elementProp
        Element elementProp = document.createElement("elementProp");
        elementProp.setAttribute("name", "ThreadGroup.main_controller");
        elementProp.setAttribute("elementType", "com.blazemeter.jmeter.control.VirtualUserController");
        threadGroup.appendChild(elementProp);


        threadGroup.appendChild(createStringProp(document, "ThreadGroup.on_sample_error", "continue"));
        threadGroup.appendChild(createStringProp(document, "TargetLevel", "2"));
        threadGroup.appendChild(createStringProp(document, "RampUp", "12"));
        threadGroup.appendChild(createStringProp(document, "Steps", "2"));
        threadGroup.appendChild(createStringProp(document, "Hold", "12"));
        threadGroup.appendChild(createStringProp(document, "LogFilename", ""));
        threadGroup.appendChild(createStringProp(document, "Iterations", "1"));
        threadGroup.appendChild(createStringProp(document, "Unit", "S"));
    }

    private void processTimer(Element element) {
        /*
        <kg.apc.jmeter.timers.VariableThroughputTimer guiclass="kg.apc.jmeter.timers.VariableThroughputTimerGui" testclass="kg.apc.jmeter.timers.VariableThroughputTimer" testname="jp@gc - Throughput Shaping Timer" enabled="true">
          <collectionProp name="load_profile">
            <collectionProp name="140409499">
              <stringProp name="49">1</stringProp>
              <stringProp name="49">1</stringProp>
              <stringProp name="1570">13</stringProp>
            </collectionProp>
          </collectionProp>
        </kg.apc.jmeter.timers.VariableThroughputTimer>
         */
        Document document = element.getOwnerDocument();


        Node timerParent = element.getNextSibling();
        while (!(timerParent instanceof Element)) {
            timerParent = timerParent.getNextSibling();
        }

        Element timer = document.createElement(VARIABLE_THROUGHPUT_TIMER);
        timer.setAttribute("guiclass", VARIABLE_THROUGHPUT_TIMER + "Gui");
        timer.setAttribute("testclass", VARIABLE_THROUGHPUT_TIMER);
        timer.setAttribute("testname", "jp@gc - Throughput Shaping Timer");
        timer.setAttribute("enabled", "true");

        Element collectionProp = document.createElement("collectionProp");
        collectionProp.setAttribute("name", "load_profile");
        Element childCollectionProp = document.createElement("collectionProp");
        childCollectionProp.setAttribute("name", "140409499");
        childCollectionProp.appendChild(createStringProp(document, "49", "1"));
        childCollectionProp.appendChild(createStringProp(document, "49", "1"));
        childCollectionProp.appendChild(createStringProp(document, "1570", "10"));
        collectionProp.appendChild(childCollectionProp);
        timer.appendChild(collectionProp);
        timerParent.appendChild(timer);
    }

    private Element createStringProp(Document document, String name, String value) {
        Element unit = document.createElement(STRING_PROP);
        unit.setAttribute("name", name);
        unit.appendChild(document.createTextNode(value));
        return unit;
    }

    private void processConcurrencyThreadGroup(Element concurrencyThreadGroup) {
        if (concurrencyThreadGroup.getChildNodes().getLength() > 0) {
            final NodeList childNodes = concurrencyThreadGroup.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (!valid(ele)) {
                        continue;
                    }

                    if (nodeNameEquals(ele, STRING_PROP)) {
                        parseStringProp(ele);
                    }
                }
            }
        }
    }

    private void parseStringProp(Element stringProp) {
        if (stringProp.getChildNodes().getLength() > 0) {
            stringProp.getFirstChild().setNodeValue("1");
        }
    }

    private boolean nodeNameEquals(Node node, String desiredName) {
        return desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName());
    }

    private boolean valid(Element ele) {
        return StringUtils.isBlank(ele.getAttribute("enabled")) || Boolean.parseBoolean(ele.getAttribute("enabled"));
    }

    private void removeChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }
}
