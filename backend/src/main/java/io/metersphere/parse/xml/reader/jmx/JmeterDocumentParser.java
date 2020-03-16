package io.metersphere.parse.xml.reader.jmx;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.KafkaProperties;
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
import java.io.StringWriter;

public class JmeterDocumentParser implements DocumentParser {
    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String STRING_PROP = "stringProp";
    private final static String COLLECTION_PROP = "collectionProp";
    private final static String CONCURRENCY_THREAD_GROUP = "com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup";
    private final static String VARIABLE_THROUGHPUT_TIMER = "kg.apc.jmeter.timers.VariableThroughputTimer";
    private final static String THREAD_GROUP = "ThreadGroup";
    private final static String BACKEND_LISTENER = "BackendListener";

    private EngineContext context;

    @Override
    public String parse(EngineContext context, Document document) throws Exception {
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

        return documentToString(document);
    }

    private String documentToString(Document document) throws TransformerException {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
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
                        processCheckoutTimer(ele);
                        processCheckoutBackendListener(ele);
                    } else if (nodeNameEquals(ele, VARIABLE_THROUGHPUT_TIMER)) {
                        processVariableThroughputTimer(ele);
                    } else if (nodeNameEquals(ele, THREAD_GROUP)) {
                        processThreadGroup(ele);
                        //
                        processConcurrencyThreadGroup(ele);
                        processCheckoutTimer(ele);
                        processCheckoutBackendListener(ele);
                    } else if (nodeNameEquals(ele, BACKEND_LISTENER)) {
                        processBackendListener(ele);
                    }
                }
            }
        }
    }

    private void processBackendListener(Element backendListener) {
        KafkaProperties kafkaProperties = CommonBeanFactory.getBean(KafkaProperties.class);
        Document document = backendListener.getOwnerDocument();
        // 清空child
        removeChildren(backendListener);
        backendListener.appendChild(createStringProp(document, "classname", "io.github.rahulsinghai.jmeter.backendlistener.kafka.KafkaBackendClient"));
        backendListener.appendChild(createStringProp(document, "QUEUE_SIZE", "5000"));
        // elementProp
        Element elementProp = document.createElement("elementProp");
        elementProp.setAttribute("name", "arguments");
        elementProp.setAttribute("elementType", "Arguments");
        elementProp.setAttribute("guiclass", "ArgumentsPanel");
        elementProp.setAttribute("testclass", "Arguments");
        elementProp.setAttribute("enabled", "true");
        Element collectionProp = document.createElement("collectionProp");
        collectionProp.setAttribute("name", "Arguments.arguments");
        collectionProp.appendChild(createKafkaProp(document, "kafka.acks", kafkaProperties.getAcks()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.bootstrap.servers", kafkaProperties.getBootstrapServers()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.topic", kafkaProperties.getTopic()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.sample.filter", kafkaProperties.getSampleFilter()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.fields", kafkaProperties.getFields()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.test.mode", kafkaProperties.getTestMode()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.parse.all.req.headers", kafkaProperties.getParseAllReqHeaders()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.parse.all.res.headers", kafkaProperties.getParseAllResHeaders()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.timestamp", kafkaProperties.getTimestamp()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.compression.type", kafkaProperties.getCompressionType()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.enabled", kafkaProperties.getSsl().getEnabled()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.key.password", kafkaProperties.getSsl().getKeyPassword()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.keystore.location", kafkaProperties.getSsl().getKeystoreLocation()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.keystore.password", kafkaProperties.getSsl().getKeystorePassword()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.truststore.location", kafkaProperties.getSsl().getTruststoreLocation()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.truststore.password", kafkaProperties.getSsl().getTruststorePassword()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.enabled.protocols", kafkaProperties.getSsl().getEnabledProtocols()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.keystore.type", kafkaProperties.getSsl().getKeystoreType()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.protocol", kafkaProperties.getSsl().getProtocol()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.provider", kafkaProperties.getSsl().getProvider()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.ssl.truststore.type", kafkaProperties.getSsl().getTruststoreType()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.batch.size", kafkaProperties.getBatchSize()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.client.id", kafkaProperties.getClientId()));
        collectionProp.appendChild(createKafkaProp(document, "kafka.connections.max.idle.ms", kafkaProperties.getConnectionsMaxIdleMs()));

        elementProp.appendChild(collectionProp);
        // set elementProp
        backendListener.appendChild(elementProp);
    }

    private Element createKafkaProp(Document document, String name, String value) {
        Element eleProp = document.createElement("elementProp");
        eleProp.setAttribute("name", name);
        eleProp.setAttribute("elementType", "Argument");
        eleProp.appendChild(createStringProp(document, "Argument.name", name));
        eleProp.appendChild(createStringProp(document, "Argument.value", value));
        eleProp.appendChild(createStringProp(document, "Argument.metadata", "="));
        return eleProp;
    }

    private void processCheckoutBackendListener(Element element) {
        Document document = element.getOwnerDocument();

        Node listenerParent = element.getNextSibling();
        while (!(listenerParent instanceof Element)) {
            listenerParent = listenerParent.getNextSibling();
        }

        NodeList childNodes = listenerParent.getChildNodes();
        for (int i = 0, l = childNodes.getLength(); i < l; i++) {
            Node item = childNodes.item(i);
            if (nodeNameEquals(item, BACKEND_LISTENER)) {
                // 如果已经存在，不再添加
                return;
            }
        }

        // add class name
        Element backendListener = document.createElement(BACKEND_LISTENER);
        backendListener.setAttribute("guiclass", "BackendListenerGui");
        backendListener.setAttribute("testclass", "BackendListener");
        backendListener.setAttribute("testname", "Backend Listener");
        backendListener.setAttribute("enabled", "true");
        listenerParent.appendChild(backendListener);
        listenerParent.appendChild(document.createElement(HASH_TREE_ELEMENT));
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
        // todo 单位是S 要修改 成M
        threadGroup.appendChild(createStringProp(document, "Unit", "S"));
    }

    private void processCheckoutTimer(Element element) {
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

        NodeList childNodes = timerParent.getChildNodes();
        for (int i = 0, l = childNodes.getLength(); i < l; i++) {
            Node item = childNodes.item(i);
            if (nodeNameEquals(item, VARIABLE_THROUGHPUT_TIMER)) {
                // 如果已经存在，不再添加
                return;
            }
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
        // 添加一个空的hashTree
        timerParent.appendChild(document.createElement(HASH_TREE_ELEMENT));
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
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (invalid(ele)) {
                        continue;
                    }

                    // TODO kg.apc.jmeter.timers.VariableThroughputTimer的stringProp的name属性是动态的
                    if (nodeNameEquals(ele, COLLECTION_PROP)) {
                        NodeList eleChildNodes = ele.getChildNodes();
                        for (int j = 0; j < eleChildNodes.getLength(); j++) {
                            Node item = eleChildNodes.item(j);
                            if (nodeNameEquals(item, COLLECTION_PROP)) {
                                int stringPropCount = 0;
                                NodeList itemChildNodes = item.getChildNodes();
                                for (int k = 0; k < itemChildNodes.getLength(); k++) {
                                    Node prop = itemChildNodes.item(k);
                                    if (nodeNameEquals(prop, STRING_PROP)) {
                                        if (stringPropCount < 2) {
                                            stringPropCount++;
                                        } else {
                                            stringPropCount = 0;
                                            prop.getFirstChild().setNodeValue(context.getProperty("duration").toString());
                                            continue;
                                        }
                                        prop.getFirstChild().setNodeValue(context.getProperty("rpsLimit").toString());
                                    }
                                }
                            }

                        }
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

    private void removeChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }
}
