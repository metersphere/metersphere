package io.metersphere.performance.parse.xml.reader.jmx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.ScriptEngineUtils;
import io.metersphere.config.KafkaProperties;
import io.metersphere.i18n.Translator;
import io.metersphere.performance.engine.EngineContext;
import io.metersphere.performance.parse.xml.reader.DocumentParser;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashSet;
import java.util.List;

public class JmeterDocumentParser implements DocumentParser {
    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String TEST_PLAN = "TestPlan";
    private final static String STRING_PROP = "stringProp";
    private final static String COLLECTION_PROP = "collectionProp";
    private final static String CONCURRENCY_THREAD_GROUP = "com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup";
    private final static String VARIABLE_THROUGHPUT_TIMER = "kg.apc.jmeter.timers.VariableThroughputTimer";
    private final static String THREAD_GROUP = "ThreadGroup";
    private final static String BACKEND_LISTENER = "BackendListener";
    private final static String CONFIG_TEST_ELEMENT = "ConfigTestElement";
    private final static String DNS_CACHE_MANAGER = "DNSCacheManager";
    private final static String ARGUMENTS = "Arguments";
    private final static String RESPONSE_ASSERTION = "ResponseAssertion";
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
                    } else if (nodeNameEquals(ele, TEST_PLAN)) {
                        processSetupTestPlan(ele);
                        processTearDownTestPlan(ele);
                        processCheckoutConfigTestElement(ele);
                        processCheckoutDnsCacheManager(ele);
                        processCheckoutArguments(ele);
                        processCheckoutResponseAssertion(ele);
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
                    } else if (nodeNameEquals(ele, CONFIG_TEST_ELEMENT)) {
                        processConfigTestElement(ele);
                    } else if (nodeNameEquals(ele, DNS_CACHE_MANAGER)) {
                        // todo dns cache manager bug:  https://bz.apache.org/bugzilla/show_bug.cgi?id=63858
                        // processDnsCacheManager(ele);
                    } else if (nodeNameEquals(ele, ARGUMENTS)) {
                        processArguments(ele);
                    } else if (nodeNameEquals(ele, RESPONSE_ASSERTION)) {
                        processResponseAssertion(ele);
                    }
                }
            }
        }
    }

    private void processResponseAssertion(Element element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element && nodeNameEquals(item, "collectionProp")) {
                Document document = item.getOwnerDocument();
                Object params = context.getProperty("statusCode");
                if (params instanceof List) {
                    HashSet set = new HashSet((List) params);
                    for (Object p : set) {
                        item.appendChild(createStringProp(document, p.toString(), p.toString()));
                    }
                }
            }
        }
    }

    private void processCheckoutResponseAssertion(Element element) {
        if (context.getProperty("statusCode") == null || JSON.parseArray(context.getProperty("statusCode").toString()).size() == 0) {
            return;
        }
        Document document = element.getOwnerDocument();

        Node hashTree = element.getNextSibling();
        while (!(hashTree instanceof Element)) {
            hashTree = hashTree.getNextSibling();
        }

        NodeList childNodes = hashTree.getChildNodes();
        for (int i = 0, l = childNodes.getLength(); i < l; i++) {
            Node item = childNodes.item(i);
            if (nodeNameEquals(item, RESPONSE_ASSERTION)) {
                // 如果已经存在，不再添加
                removeChildren(item);
                Element collectionProp = document.createElement(COLLECTION_PROP);
                collectionProp.setAttribute("name", "Asserion.test_strings");
                //
                item.appendChild(collectionProp);
                item.appendChild(createStringProp(document, "Assertion.custom_message", ""));
                item.appendChild(createStringProp(document, "Assertion.test_field", "Assertion.response_code"));
                item.appendChild(createBoolProp(document, "Assertion.assume_success", true));
                item.appendChild(createIntProp(document, "Assertion.test_type", 40));
                return;
            }
        }
        /*
        <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Response Assertion" enabled="true">
          <collectionProp name="Asserion.test_strings">
            <stringProp name="50548">301</stringProp>
            <stringProp name="49586">200</stringProp>
          </collectionProp>
          <stringProp name="Assertion.custom_message"></stringProp>
          <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
          <boolProp name="Assertion.assume_success">false</boolProp>
          <intProp name="Assertion.test_type">33</intProp>
        </ResponseAssertion>
         */

        // add class name
        Element responseAssertion = document.createElement(RESPONSE_ASSERTION);
        responseAssertion.setAttribute("guiclass", "AssertionGui");
        responseAssertion.setAttribute("testclass", "ResponseAssertion");
        responseAssertion.setAttribute("testname", "Response Assertion");
        responseAssertion.setAttribute("enabled", "true");
        Element collectionProp = document.createElement(COLLECTION_PROP);
        collectionProp.setAttribute("name", "Asserion.test_strings");
        //
        responseAssertion.appendChild(collectionProp);
        responseAssertion.appendChild(createStringProp(document, "Assertion.custom_message", ""));
        responseAssertion.appendChild(createStringProp(document, "Assertion.test_field", "Assertion.response_code"));
        responseAssertion.appendChild(createBoolProp(document, "Assertion.assume_success", true));
        responseAssertion.appendChild(createIntProp(document, "Assertion.test_type", 40));
        hashTree.appendChild(responseAssertion);
        hashTree.appendChild(document.createElement(HASH_TREE_ELEMENT));
    }

    private void processCheckoutArguments(Element ele) {
        if (context.getProperty("params") == null || JSON.parseArray(context.getProperty("params").toString()).size() == 0) {
            return;
        }
        Node hashTree = ele.getNextSibling();
        while (!(hashTree instanceof Element)) {
            hashTree = hashTree.getNextSibling();
        }

        NodeList childNodes = hashTree.getChildNodes();
        for (int i = 0, size = childNodes.getLength(); i < size; i++) {
            Node item = childNodes.item(i);
            if (nodeNameEquals(item, ARGUMENTS)) {
                // 已经存在不再添加
                return;
            }
        }
        /*
        <Arguments guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL_1" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL_1</stringProp>
            <stringProp name="Argument.value">rddev2.fit2cloud.com</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
        </collectionProp>
      </Arguments>
         */

        Document document = ele.getOwnerDocument();
        Element element = document.createElement(ARGUMENTS);
        element.setAttribute("guiclass", "ArgumentsPanel");
        element.setAttribute("testclass", "Arguments");
        element.setAttribute("testname", "User Defined Variables");
        element.setAttribute("enabled", "true");
        Element collectionProp = document.createElement(COLLECTION_PROP);
        collectionProp.setAttribute("name", "Arguments.arguments");
        element.appendChild(collectionProp);
        hashTree.appendChild(element);
        // 空的 hashTree
        hashTree.appendChild(document.createElement(HASH_TREE_ELEMENT));
    }

    private void processCheckoutDnsCacheManager(Element ele) {
        if (context.getProperty("domains") == null || JSON.parseArray(context.getProperty("domains").toString()).size() == 0) {
            return;
        }
        Node hashTree = ele.getNextSibling();
        while (!(hashTree instanceof Element)) {
            hashTree = hashTree.getNextSibling();
        }

        NodeList childNodes = hashTree.getChildNodes();
        for (int i = 0, size = childNodes.getLength(); i < size; i++) {
            Node item = childNodes.item(i);
            if (nodeNameEquals(item, DNS_CACHE_MANAGER)) {
                // 已经存在不再添加
                return;
            }
        }
         /*
        <DNSCacheManager guiclass="DNSCachePanel" testclass="DNSCacheManager" testname="DNS Cache Manager" enabled="true">
        <collectionProp name="DNSCacheManager.servers"/>
        <collectionProp name="DNSCacheManager.hosts">
          <elementProp name="baiud.com" elementType="StaticHost">
            <stringProp name="StaticHost.Name">baiud.com</stringProp>
            <stringProp name="StaticHost.Address">172.16.10.187</stringProp>
          </elementProp>
        </collectionProp>
        <boolProp name="DNSCacheManager.clearEachIteration">true</boolProp>
        <boolProp name="DNSCacheManager.isCustomResolver">true</boolProp>
      </DNSCacheManager>
         */

        Document document = ele.getOwnerDocument();
        Element element = document.createElement(DNS_CACHE_MANAGER);
        element.setAttribute("guiclass", "DNSCachePanel");
        element.setAttribute("testclass", "DNSCacheManager");
        element.setAttribute("testname", "DNS Cache Manager");
        element.setAttribute("enabled", "true");
        Element collectionProp = document.createElement(COLLECTION_PROP);
        collectionProp.setAttribute("name", "DNSCacheManager.servers");
        element.appendChild(collectionProp);

        Element collectionProp2 = document.createElement(COLLECTION_PROP);
        collectionProp2.setAttribute("name", "DNSCacheManager.hosts");
        element.appendChild(collectionProp2);

        element.appendChild(createBoolProp(document, "DNSCacheManager.clearEachIteration", true));
        element.appendChild(createBoolProp(document, "DNSCacheManager.isCustomResolver", true));

        hashTree.appendChild(element);
        // 空的 hashTree
        hashTree.appendChild(document.createElement(HASH_TREE_ELEMENT));
    }

    private void processCheckoutConfigTestElement(Element ele) {
        if (context.getProperty("timeout") == null || StringUtils.isBlank(context.getProperty("timeout").toString())) {
            return;
        }
        Node hashTree = ele.getNextSibling();
        while (!(hashTree instanceof Element)) {
            hashTree = hashTree.getNextSibling();
        }

        NodeList childNodes = hashTree.getChildNodes();
        for (int i = 0, size = childNodes.getLength(); i < size; i++) {
            Node item = childNodes.item(i);
            if (nodeNameEquals(item, CONFIG_TEST_ELEMENT)) {
                // 已经存在不再添加
                return;
            }
        }
/*
        <ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP Request Defaults" enabled="true">
        <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" enabled="true">
          <collectionProp name="Arguments.arguments"/>
        </elementProp>
        <stringProp name="HTTPSampler.domain"></stringProp>
        <stringProp name="HTTPSampler.port"></stringProp>
        <stringProp name="HTTPSampler.protocol"></stringProp>
        <stringProp name="HTTPSampler.contentEncoding"></stringProp>
        <stringProp name="HTTPSampler.path"></stringProp>
        <boolProp name="HTTPSampler.image_parser">true</boolProp>
        <boolProp name="HTTPSampler.concurrentDwn">true</boolProp>
        <stringProp name="HTTPSampler.concurrentPool">6</stringProp>
        <stringProp name="HTTPSampler.connect_timeout">30000</stringProp>
        <stringProp name="HTTPSampler.response_timeout"></stringProp>
        </ConfigTestElement>
         */
        Document document = ele.getOwnerDocument();
        Element element = document.createElement(CONFIG_TEST_ELEMENT);
        element.setAttribute("guiclass", "HttpDefaultsGui");
        element.setAttribute("testclass", "ConfigTestElement");
        element.setAttribute("testname", "HTTP Request Defaults");
        element.setAttribute("enabled", "true");
        Element elementProp = document.createElement("elementProp");
        elementProp.setAttribute("name", "HTTPsampler.Arguments");
        elementProp.setAttribute("elementType", "Arguments");
        elementProp.setAttribute("guiclass", "HTTPArgumentsPanel");
        elementProp.setAttribute("testclass", "Arguments");
        elementProp.setAttribute("enabled", "true");
        Element collectionProp = document.createElement(COLLECTION_PROP);
        collectionProp.setAttribute("name", "Arguments.arguments");
        elementProp.appendChild(collectionProp);
        element.appendChild(elementProp);
        element.appendChild(createStringProp(document, "HTTPSampler.domain", ""));
        element.appendChild(createStringProp(document, "HTTPSampler.port", ""));
        element.appendChild(createStringProp(document, "HTTPSampler.protocol", ""));
        element.appendChild(createStringProp(document, "HTTPSampler.contentEncoding", ""));
        element.appendChild(createStringProp(document, "HTTPSampler.path", ""));
        element.appendChild(createStringProp(document, "HTTPSampler.concurrentPool", "6"));
        element.appendChild(createStringProp(document, "HTTPSampler.connect_timeout", ""));
        element.appendChild(createStringProp(document, "HTTPSampler.response_timeout", ""));
        element.appendChild(createBoolProp(document, "HTTPSampler.image_parser", true));
        element.appendChild(createBoolProp(document, "HTTPSampler.concurrentDwn", true));
        hashTree.appendChild(element);
        // 空的 hashTree
        hashTree.appendChild(document.createElement(HASH_TREE_ELEMENT));
    }

    private void processArguments(Element ele) {
        NodeList childNodes = ele.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element && nodeNameEquals(item, "collectionProp")) {
                //
                Document document = item.getOwnerDocument();
                Object params = context.getProperty("params");
                if (params instanceof List) {
                    for (Object p : (List) params) {
                        JSONObject jsonObject = JSON.parseObject(p.toString());
                        if (!jsonObject.getBooleanValue("enable")) {
                            continue;
                        }
                        Element elementProp = document.createElement("elementProp");
                        elementProp.setAttribute("name", jsonObject.getString("name"));
                        elementProp.setAttribute("elementType", "Argument");
                        elementProp.appendChild(createStringProp(document, "Argument.name", jsonObject.getString("name")));
                        // 处理 mock data
                        String value = jsonObject.getString("value");
                        elementProp.appendChild(createStringProp(document, "Argument.value", ScriptEngineUtils.calculate(value)));
                        elementProp.appendChild(createStringProp(document, "Argument.metadata", "="));
                        item.appendChild(elementProp);
                    }
                }
            }
        }

    }

    private void processDnsCacheManager(Element ele) {

        Object domains = context.getProperty("domains");
        if (!(domains instanceof List)) {
            return;
        }
        if (((List) domains).size() == 0) {
            return;
        }
        NodeList childNodes = ele.getChildNodes();
        for (int i = 0, size = childNodes.getLength(); i < size; i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element && nodeNameEquals(item, "collectionProp")
                    && org.apache.commons.lang3.StringUtils.equals(((Element) item).getAttribute("name"), "DNSCacheManager.hosts")) {

                Document document = item.getOwnerDocument();
                for (Object d : (List) domains) {
                    JSONObject jsonObject = JSON.parseObject(d.toString());
                    if (!jsonObject.getBooleanValue("enable")) {
                        continue;
                    }
                    Element elementProp = document.createElement("elementProp");
                    elementProp.setAttribute("name", jsonObject.getString("domain"));
                    elementProp.setAttribute("elementType", "StaticHost");
                    elementProp.appendChild(createStringProp(document, "StaticHost.Name", jsonObject.getString("domain")));
                    elementProp.appendChild(createStringProp(document, "StaticHost.Address", jsonObject.getString("ip")));
                    item.appendChild(elementProp);
                }
            }
            if (item instanceof Element && nodeNameEquals(item, "boolProp")
                    && org.apache.commons.lang3.StringUtils.equals(((Element) item).getAttribute("name"), "DNSCacheManager.isCustomResolver")) {
                item.getFirstChild().setNodeValue("true");
            }
        }

    }

    private void processConfigTestElement(Element ele) {

        NodeList childNodes = ele.getChildNodes();
        for (int i = 0, size = childNodes.getLength(); i < size; i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element && nodeNameEquals(item, STRING_PROP)
                    && StringUtils.equals(((Element) item).getAttribute("name"), "HTTPSampler.connect_timeout")) {
                if (context.getProperty("timeout") != null) {
                    removeChildren(item);
                    item.appendChild(ele.getOwnerDocument().createTextNode(context.getProperty("timeout").toString()));
                }
            }
        }
    }

    private void processSetupTestPlan(Element ele) {
        Document document = ele.getOwnerDocument();
        Node hashTree = ele.getNextSibling();
        while (!(hashTree instanceof Element)) {
            hashTree = hashTree.getNextSibling();
        }

        KafkaProperties kafkaProperties = CommonBeanFactory.getBean(KafkaProperties.class);
        String bootstrapServers = kafkaProperties.getBootstrapServers();
        String[] servers = StringUtils.split(bootstrapServers, ",");
        for (String s : servers) {
            String[] ipAndPort = StringUtils.split(s, ":");
            Element setupElement = document.createElement("SetupThreadGroup");
            setupElement.setAttribute("guiclass", "SetupThreadGroupGui");
            setupElement.setAttribute("testclass", "SetupThreadGroup");
            setupElement.setAttribute("testname", "setUp Thread Group");
            setupElement.setAttribute("enabled", "true");
            setupElement.appendChild(createStringProp(document, "ThreadGroup.on_sample_error", "stoptestnow"));
            Element elementProp = document.createElement("elementProp");
            elementProp.setAttribute("name", "ThreadGroup.main_controller");
            elementProp.setAttribute("elementType", "LoopController");
            elementProp.setAttribute("guiclass", "LoopControlPanel");
            elementProp.setAttribute("testclass", "LoopController");
            elementProp.setAttribute("testname", "Loop Controller");
            elementProp.setAttribute("enabled", "true");
            elementProp.appendChild(createBoolProp(document, "LoopController.continue_forever", false));
            elementProp.appendChild(createIntProp(document, "LoopController.loops", 1));
            setupElement.appendChild(elementProp);
            setupElement.appendChild(createStringProp(document, "ThreadGroup.num_threads", "1"));
            setupElement.appendChild(createStringProp(document, "ThreadGroup.ramp_time", "1"));
            setupElement.appendChild(createStringProp(document, "ThreadGroup.duration", ""));
            setupElement.appendChild(createStringProp(document, "ThreadGroup.delay", ""));
            setupElement.appendChild(createBoolProp(document, "ThreadGroup.scheduler", false));
            setupElement.appendChild(createBoolProp(document, "ThreadGroup.same_user_on_next_iteration", true));
            hashTree.appendChild(setupElement);

            Element setupHashTree = document.createElement(HASH_TREE_ELEMENT);

            Element tcpSampler = document.createElement("TCPSampler");
            tcpSampler.setAttribute("guiclass", "TCPSamplerGui");
            tcpSampler.setAttribute("testclass", "TCPSampler");
            tcpSampler.setAttribute("testname", "TCP Sampler");
            tcpSampler.setAttribute("enabled", "true");
            tcpSampler.appendChild(createStringProp(document, "TCPSampler.classname", "TCPClientImpl"));
            tcpSampler.appendChild(createStringProp(document, "TCPSampler.server", ipAndPort[0]));
            tcpSampler.appendChild(createBoolProp(document, "TCPSampler.reUseConnection", true));
            tcpSampler.appendChild(createStringProp(document, "TCPSampler.port", ipAndPort[1]));
            tcpSampler.appendChild(createBoolProp(document, "TCPSampler.nodelay", false));
            tcpSampler.appendChild(createStringProp(document, "TCPSampler.timeout", "100"));
            tcpSampler.appendChild(createStringProp(document, "TCPSampler.ctimeout", "100"));
            tcpSampler.appendChild(createStringProp(document, "TCPSampler.request", "1010"));
            tcpSampler.appendChild(createBoolProp(document, "TCPSampler.closeConnection", false));
            tcpSampler.appendChild(createStringProp(document, "TCPSampler.EolByte", "0"));
            tcpSampler.appendChild(createStringProp(document, "ConfigTestElement.username", ""));
            tcpSampler.appendChild(createStringProp(document, "ConfigTestElement.password", ""));

            Element tcpSamplerHashTree = document.createElement(HASH_TREE_ELEMENT);

            Element responseAssertion = document.createElement("ResponseAssertion");
            responseAssertion.setAttribute("guiclass", "AssertionGui");
            responseAssertion.setAttribute("testclass", "ResponseAssertion");
            responseAssertion.setAttribute("testname", "Response Assertion");
            responseAssertion.setAttribute("enabled", "true");
            Element collectionProp = document.createElement("collectionProp");
            collectionProp.setAttribute("name", "Asserion.test_strings");
            collectionProp.appendChild(createStringProp(document, "49586", "200"));
            responseAssertion.appendChild(collectionProp);
            responseAssertion.appendChild(createStringProp(document, "Assertion.custom_message", ""));
            responseAssertion.appendChild(createStringProp(document, "Assertion.test_field", "Assertion.response_code"));
            responseAssertion.appendChild(createBoolProp(document, "Assertion.assume_success", false));
            responseAssertion.appendChild(createIntProp(document, "Assertion.test_type", 8));
            tcpSamplerHashTree.appendChild(responseAssertion);
            // 添加空的hashtree
            tcpSamplerHashTree.appendChild(document.createElement(HASH_TREE_ELEMENT));

            setupHashTree.appendChild(tcpSampler);
            setupHashTree.appendChild(tcpSamplerHashTree);

            hashTree.appendChild(setupHashTree);
        }
    }

    private void processTearDownTestPlan(Element ele) {
        /*<boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>*/
        Document document = ele.getOwnerDocument();
        Element tearDownSwitch = createBoolProp(document, "TestPlan.tearDown_on_shutdown", true);
        ele.appendChild(tearDownSwitch);

        Node hashTree = ele.getNextSibling();
        while (!(hashTree instanceof Element)) {
            hashTree = hashTree.getNextSibling();
        }
        /*
        <PostThreadGroup guiclass="PostThreadGroupGui" testclass="PostThreadGroup" testname="tearDown Thread Group" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">1</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">1</stringProp>
        <stringProp name="ThreadGroup.ramp_time">1</stringProp>
        <boolProp name="ThreadGroup.scheduler">false</boolProp>
        <stringProp name="ThreadGroup.duration"></stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
      </PostThreadGroup>
         */
        Element tearDownElement = document.createElement("PostThreadGroup");
        tearDownElement.setAttribute("guiclass", "PostThreadGroupGui");
        tearDownElement.setAttribute("testclass", "PostThreadGroup");
        tearDownElement.setAttribute("testname", "tearDown Thread Group");
        tearDownElement.setAttribute("enabled", "true");
        tearDownElement.appendChild(createStringProp(document, "ThreadGroup.on_sample_error", "continue"));
        tearDownElement.appendChild(createStringProp(document, "ThreadGroup.num_threads", "1"));
        tearDownElement.appendChild(createStringProp(document, "ThreadGroup.ramp_time", "1"));
        tearDownElement.appendChild(createStringProp(document, "ThreadGroup.duration", ""));
        tearDownElement.appendChild(createStringProp(document, "ThreadGroup.delay", ""));
        tearDownElement.appendChild(createBoolProp(document, "ThreadGroup.scheduler", false));
        tearDownElement.appendChild(createBoolProp(document, "ThreadGroup.same_user_on_next_iteration", true));
        Element elementProp = document.createElement("elementProp");
        elementProp.setAttribute("name", "ThreadGroup.main_controller");
        elementProp.setAttribute("elementType", "LoopController");
        elementProp.setAttribute("guiclass", "LoopControlPanel");
        elementProp.setAttribute("testclass", "LoopController");
        elementProp.setAttribute("testname", "Loop Controller");
        elementProp.setAttribute("enabled", "true");
        elementProp.appendChild(createBoolProp(document, "LoopController.continue_forever", false));
        elementProp.appendChild(createStringProp(document, "LoopController.loops", "1"));
        tearDownElement.appendChild(elementProp);
        hashTree.appendChild(tearDownElement);

        Element tearDownHashTree = document.createElement(HASH_TREE_ELEMENT);
        /*
        <OnceOnlyController guiclass="OnceOnlyControllerGui" testclass="OnceOnlyController" testname="Once Only Controller" enabled="true"/>
         */
        Element onceOnlyController = document.createElement("OnceOnlyController");
        onceOnlyController.setAttribute("guiclass", "OnceOnlyControllerGui");
        onceOnlyController.setAttribute("testclass", "OnceOnlyController");
        onceOnlyController.setAttribute("testname", "Once Only Controller");
        onceOnlyController.setAttribute("enabled", "true");
        tearDownHashTree.appendChild(onceOnlyController);
         /*
                <hashTree>
          <DebugSampler guiclass="TestBeanGUI" testclass="DebugSampler" testname="Debug Sampler" enabled="true">
            <boolProp name="displayJMeterProperties">false</boolProp>
            <boolProp name="displayJMeterVariables">true</boolProp>
            <boolProp name="displaySystemProperties">false</boolProp>
          </DebugSampler>
          <hashTree/>
        </hashTree>
         */
        Element onceOnlyHashTree = document.createElement(HASH_TREE_ELEMENT);
        Element debugSampler = document.createElement("DebugSampler");
        debugSampler.setAttribute("guiclass", "TestBeanGUI");
        debugSampler.setAttribute("testclass", "DebugSampler");
        debugSampler.setAttribute("testname", "Debug Sampler");
        debugSampler.setAttribute("enabled", "true");
        debugSampler.appendChild(createBoolProp(document, "displayJMeterProperties", false));
        debugSampler.appendChild(createBoolProp(document, "displayJMeterVariables", true));
        debugSampler.appendChild(createBoolProp(document, "displaySystemProperties", false));
        onceOnlyHashTree.appendChild(debugSampler);
        // 添加空的 hashTree
        onceOnlyHashTree.appendChild(document.createElement(HASH_TREE_ELEMENT));
        tearDownHashTree.appendChild(onceOnlyHashTree);
        hashTree.appendChild(tearDownHashTree);
        // 添加backend listener
        processCheckoutBackendListener(tearDownElement);
    }

    private Element createBoolProp(Document document, String name, boolean value) {
        Element tearDownSwitch = document.createElement("boolProp");
        tearDownSwitch.setAttribute("name", name);
        tearDownSwitch.appendChild(document.createTextNode(String.valueOf(value)));
        return tearDownSwitch;
    }

    private Element createIntProp(Document document, String name, int value) {
        Element tearDownSwitch = document.createElement("intProp");
        tearDownSwitch.setAttribute("name", name);
        tearDownSwitch.appendChild(document.createTextNode(String.valueOf(value)));
        return tearDownSwitch;
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
        // 添加关联关系 test.id test.name test.startTime test.reportId
        collectionProp.appendChild(createKafkaProp(document, "test.id", context.getTestId()));
        collectionProp.appendChild(createKafkaProp(document, "test.name", context.getTestName()));
        collectionProp.appendChild(createKafkaProp(document, "test.startTime", context.getStartTime().toString()));
        collectionProp.appendChild(createKafkaProp(document, "test.reportId", context.getReportId()));

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
        // 检查 threadgroup 后面的hashtree是否为空
        Node hashTree = threadGroup.getNextSibling();
        while (!(hashTree instanceof Element)) {
            hashTree = hashTree.getNextSibling();
        }
        if (!hashTree.hasChildNodes()) {
            MSException.throwException(Translator.get("jmx_content_valid"));
        }
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
        // 持续时长
        String duration = context.getProperty("duration").toString();
        String rampUp = context.getProperty("RampUp").toString();
        int realHold = Integer.parseInt(duration) - Integer.parseInt(rampUp);
        threadGroup.appendChild(createStringProp(document, "ThreadGroup.on_sample_error", "continue"));
        threadGroup.appendChild(createStringProp(document, "TargetLevel", "2"));
        threadGroup.appendChild(createStringProp(document, "RampUp", "12"));
        threadGroup.appendChild(createStringProp(document, "Steps", "2"));
        threadGroup.appendChild(createStringProp(document, "Hold", String.valueOf(realHold)));
        threadGroup.appendChild(createStringProp(document, "LogFilename", ""));
        // bzm - Concurrency Thread Group "Thread Iterations Limit:" 设置为空
//        threadGroup.appendChild(createStringProp(document, "Iterations", "1"));
        threadGroup.appendChild(createStringProp(document, "Unit", "M"));
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
        if (context.getProperty("rpsLimitEnable") == null || StringUtils.equals(context.getProperty("rpsLimitEnable").toString(), "false")) {
            return;
        }
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
        String testname = concurrencyThreadGroup.getAttribute("testname");
        concurrencyThreadGroup.setAttribute("testname", testname + "-" + context.getResourceIndex());
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

                    // 设置具体的线程数
                    if (nodeNameEquals(ele, STRING_PROP) && "TargetLevel".equals(ele.getAttribute("name"))) {
                        ele.getFirstChild().setNodeValue(context.getThreadNum().toString());
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
                                            Integer duration = (Integer) context.getProperty("duration");// 传入的是分钟数, 需要转化成秒数
                                            prop.getFirstChild().setNodeValue(String.valueOf(duration * 60));
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
