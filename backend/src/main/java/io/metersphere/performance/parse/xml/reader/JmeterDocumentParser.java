package io.metersphere.performance.parse.xml.reader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.KafkaProperties;
import io.metersphere.i18n.Translator;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.performance.engine.EngineContext;
import io.metersphere.performance.parse.EngineSourceParser;
import io.metersphere.performance.parse.EngineSourceParserFactory;
import io.metersphere.service.TestResourcePoolService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class JmeterDocumentParser implements EngineSourceParser {
    private final static String HASH_TREE_ELEMENT = "hashTree";
    private final static String TEST_PLAN = "TestPlan";
    private final static String STRING_PROP = "stringProp";
    private final static String ELEMENT_PROP = "elementProp";
    private final static String BOOL_PROP = "boolProp";
    private final static String COLLECTION_PROP = "collectionProp";
    private final static String CONCURRENCY_THREAD_GROUP = "com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup";
    private final static String VARIABLE_THROUGHPUT_TIMER = "kg.apc.jmeter.timers.VariableThroughputTimer";
    private final static String THREAD_GROUP = "ThreadGroup";
    private final static String POST_THREAD_GROUP = "PostThreadGroup";
    private final static String SETUP_THREAD_GROUP = "SetupThreadGroup";
    private final static String BACKEND_LISTENER = "BackendListener";
    private final static String CONFIG_TEST_ELEMENT = "ConfigTestElement";
    private final static String ARGUMENTS = "Arguments";
    private final static String RESPONSE_ASSERTION = "ResponseAssertion";
    private final static String HTTP_SAMPLER_PROXY = "HTTPSamplerProxy";
    private final static String CSV_DATA_SET = "CSVDataSet";
    private final static String THREAD_GROUP_AUTO_STOP = "io.metersphere.jmeter.reporters.ThreadGroupAutoStop";
    private EngineContext context;

    @Override
    public byte[] parse(EngineContext context, InputStream source) throws Exception {
        this.context = context;

        Document document = EngineSourceParserFactory.getDocument(source);

        final Element jmeterTestPlan = document.getRootElement();

        List<Element> childNodes = jmeterTestPlan.elements();
        for (Element ele : childNodes) {
            // jmeterTestPlan的子元素肯定是<hashTree></hashTree>
            parseHashTree(ele);
        }

        return EngineSourceParserFactory.getBytes(document);
    }


    private void parseHashTree(Element hashTree) {
        if (invalid(hashTree)) {
            return;
        }

        if (hashTree.elements().size() > 0) {
            final List<Element> childNodes = hashTree.elements();
            for (Element ele : childNodes) {
                if (nodeNameEquals(ele, HASH_TREE_ELEMENT)) {
                    parseHashTree(ele);
                } else if (nodeNameEquals(ele, TEST_PLAN)) {
                    processCheckoutConfigTestElement(ele);
                    processCheckoutArguments(ele);
                    processCheckoutResponseAssertion(ele);
                    processCheckoutSerializeThreadgroups(ele);
                    processCheckoutBackendListener(ele);
                    processCheckoutAutoStopListener(ele);
                } else if (nodeNameEquals(ele, CONCURRENCY_THREAD_GROUP)) {
                    processThreadType(ele);
                    processThreadGroupName(ele);
                    processCheckoutTimer(ele);
                } else if (nodeNameEquals(ele, VARIABLE_THROUGHPUT_TIMER)) {
                    processVariableThroughputTimer(ele);
                } else if (nodeNameEquals(ele, THREAD_GROUP) ||
                        nodeNameEquals(ele, SETUP_THREAD_GROUP) ||
                        nodeNameEquals(ele, POST_THREAD_GROUP)) {
                    processThreadType(ele);
                    processThreadGroupName(ele);
                    processCheckoutTimer(ele);
                } else if (nodeNameEquals(ele, BACKEND_LISTENER)) {
                    processBackendListener(ele);
                } else if (nodeNameEquals(ele, CONFIG_TEST_ELEMENT)) {
                    processConfigTestElement(ele);
                } else if (nodeNameEquals(ele, ARGUMENTS)) {
                    processArguments(ele);
                } else if (nodeNameEquals(ele, RESPONSE_ASSERTION)) {
                    processResponseAssertion(ele);
                } else if (nodeNameEquals(ele, CSV_DATA_SET)) {
                    processCsvDataSet(ele);
                } else if (nodeNameEquals(ele, THREAD_GROUP_AUTO_STOP)) {
                    processAutoStopListener(ele);
                } else if (nodeNameEquals(ele, HTTP_SAMPLER_PROXY)) {
                    // 处理http上传的附件
                    processArgumentFiles(ele);
                }

            }
        }
    }

    private void processThreadType(Element ele) {
        Object threadType = context.getProperty("threadType");
        if (threadType instanceof List) {
            Object o = ((List<?>) threadType).get(0);
            ((List<?>) threadType).remove(0);
            if ("DURATION".equals(o)) {
                processThreadGroup(ele);
            }
            if ("ITERATION".equals(o)) {
                processIterationThreadGroup(ele);
            }
        } else {
            processThreadGroup(ele);
        }
    }

    private void processAutoStopListener(Element autoStopListener) {
        Object autoStopDelays = context.getProperty("autoStopDelay");
        String autoStopDelay = "30";
        if (autoStopDelays instanceof List) {
            Object o = ((List<?>) autoStopDelays).get(0);
            autoStopDelay = o.toString();
        }
        // 清空child
        removeChildren(autoStopListener);
        // 添加子元素
        appendStringProp(autoStopListener, "delay_seconds", autoStopDelay);
    }

    private void processCheckoutAutoStopListener(Element element) {
        Object autoStops = context.getProperty("autoStop");
        String autoStop = "false";
        if (autoStops instanceof List) {
            Object o = ((List<?>) autoStops).get(0);
            autoStop = o.toString();
        }
        if (!BooleanUtils.toBoolean(autoStop)) {
            return;
        }
        Element hashTree = getNextSibling(element);
        // add class name
        Element autoStopListener = hashTree.addElement(THREAD_GROUP_AUTO_STOP);
        autoStopListener.addAttribute("guiclass", "io.metersphere.jmeter.reporters.ThreadGroupAutoStopGui");
        autoStopListener.addAttribute("testclass", "io.metersphere.jmeter.reporters.ThreadGroupAutoStop");
        autoStopListener.addAttribute("testname", "MeterSphere - AutoStop Listener");
        autoStopListener.addAttribute("enabled", "true");
        hashTree.addElement(HASH_TREE_ELEMENT);
    }

    private void processCheckoutSerializeThreadgroups(Element element) {
        Object serializeThreadGroups = context.getProperty("serializeThreadGroups");
        String serializeThreadGroup = "false";
        if (serializeThreadGroups instanceof List) {
            Object o = ((List<?>) serializeThreadGroups).get(0);
            serializeThreadGroup = o.toString();
        }
        List<Element> childNodes = element.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, BOOL_PROP)) {
                String serializeName = item.attributeValue("name");
                if (StringUtils.equals(serializeName, "TestPlan.serialize_threadgroups")) {
                    item.setText(serializeThreadGroup);
                    break;
                }
            }
        }
    }

    private void processArgumentFiles(Element element) {
        List<Element> childNodes = element.elements();
        for (Element item : childNodes) {
            if (isHTTPFileArgs(item)) {
                List<Element> elementProps = item.elements();
                for (Element eleProp : elementProps) {
                    List<Element> strProps = eleProp.elements();
                    for (Element strPop : strProps) {
                        if (StringUtils.equals(strPop.attributeValue("name"), "File.path")) {
                            // 截取文件名
                            handleFilename(strPop);
                            break;
                        }
                    }
                }


            }
        }
    }

    private void handleFilename(Node item) {
        String separator = "/";
        String filename = item.getText();
        if (!StringUtils.contains(filename, "/")) {
            separator = "\\";
        }
        filename = filename.substring(filename.lastIndexOf(separator) + 1);
        item.setText(filename);
    }

    private boolean isHTTPFileArgs(Element ele) {
        return "HTTPFileArgs".equals(ele.attributeValue("elementType"));
    }

    private void processCsvDataSet(Element element) {
        List<Element> childNodes = element.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, STRING_PROP)) {
                String filenameTag = item.attributeValue("name");
                if (StringUtils.equals(filenameTag, "filename")) {
                    // 截取文件名
                    handleFilename(item);
                    // 切割CSV文件
                    splitCsvFile(item);
                    break;
                }
            }
        }
    }

    private void splitCsvFile(Node item) {
        String filename = item.getText();
        // 已经分割过的不再二次分割
        if (BooleanUtils.toBoolean(context.getSplitFlag().get(filename))) {
            return;
        }
        Object csvConfig = context.getProperty("csvConfig");
        if (csvConfig == null) {
            return;
        }
        double[] ratios = context.getRatios();
        int resourceIndex = context.getResourceIndex();
        byte[] content = context.getTestResourceFiles().get(filename);
        if (content == null) {
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(new String(content), "\n");
        if (!tokenizer.hasMoreTokens()) {
            return;
        }
        StringBuilder csv = new StringBuilder();
        Object config = ((JSONObject) csvConfig).get(filename);
        boolean csvSplit = ((JSONObject) (config)).getBooleanValue("csvSplit");
        if (!csvSplit) {
            return;
        }
        boolean csvHasHeader = ((JSONObject) (config)).getBooleanValue("csvHasHeader");
        if (csvHasHeader) {
            String header = tokenizer.nextToken();
            csv.append(header).append("\n");
        }
        int count = tokenizer.countTokens();

        long current, offset = 0;

        // 计算偏移量
        for (int k = 0; k < resourceIndex; k++) {
            offset += Math.round(count * ratios[k]);
        }

        if (resourceIndex + 1 == ratios.length) {
            current = count - offset; // 最后一个点可以分到的数量
        } else {
            current = Math.round(count * ratios[resourceIndex]); // 当前节点可以分到的数量
        }

        long index = 1;
        while (tokenizer.hasMoreTokens()) {
            if (current == 0) { // 节点一个都没有分到，把所有的数据都给这个节点（极端情况）
                String line = tokenizer.nextToken();
                csv.append(line).append("\n");
            } else {
                if (index <= offset) {
                    tokenizer.nextToken();
                    index++;
                    continue;
                }
                if (index > current + offset) {
                    break;
                }
                String line = tokenizer.nextToken();
                csv.append(line).append("\n");
            }
            index++;
        }
        // 替换文件
        context.getTestResourceFiles().put(filename, csv.toString().getBytes(StandardCharsets.UTF_8));
        context.getSplitFlag().put(filename, true);
    }

    private void processResponseAssertion(Element element) {
        List<Element> childNodes = element.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, "collectionProp")) {
                Object params = context.getProperty("statusCode");
                if (params instanceof List) {
                    HashSet set = new HashSet((List) params);
                    for (Object p : set) {
                        appendStringProp(item, p.toString(), p.toString());
                    }
                }
            }
        }
    }

    private void processCheckoutResponseAssertion(Element element) {
        if (context.getProperty("statusCode") == null || JSON.parseArray(context.getProperty("statusCode").toString()).size() == 0) {
            return;
        }
        Element hashTree = getNextSibling(element);

        List<Element> childNodes = hashTree.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, RESPONSE_ASSERTION)) {
                // 如果已经存在，不再添加
                removeChildren(item);
                Element collectionProp = item.addElement(COLLECTION_PROP);
                collectionProp.addAttribute("name", "Asserion.test_strings");
                //

                appendStringProp(item, "Assertion.custom_message", "");
                appendStringProp(item, "Assertion.test_field", "Assertion.response_code");
                appendBoolProp(item, "Assertion.assume_success", true);
                appendIntProp(item, "Assertion.test_type", 40);
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
        Element responseAssertion = hashTree.addElement(RESPONSE_ASSERTION);
        responseAssertion.addAttribute("guiclass", "AssertionGui");
        responseAssertion.addAttribute("testclass", "ResponseAssertion");
        responseAssertion.addAttribute("testname", "Response Assertion");
        responseAssertion.addAttribute("enabled", "true");
        Element collectionProp = responseAssertion.addElement(COLLECTION_PROP);
        collectionProp.addAttribute("name", "Asserion.test_strings");
        //
        appendStringProp(responseAssertion, "Assertion.custom_message", "");
        appendStringProp(responseAssertion, "Assertion.test_field", "Assertion.response_code");
        appendBoolProp(responseAssertion, "Assertion.assume_success", true);
        appendIntProp(responseAssertion, "Assertion.test_type", 40);

        hashTree.addElement(HASH_TREE_ELEMENT);
    }

    private void processCheckoutArguments(Element ele) {
        if (context.getProperty("params") == null || JSON.parseArray(context.getProperty("params").toString()).size() == 0) {
            return;
        }
        Element hashTree = getNextSibling(ele);

        List<Element> childNodes = hashTree.elements();
        for (Element item : childNodes) {
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

        Element element = hashTree.addElement(ARGUMENTS);
        element.addAttribute("guiclass", "ArgumentsPanel");
        element.addAttribute("testclass", "Arguments");
        element.addAttribute("testname", "User Defined Variables");
        element.addAttribute("enabled", "true");
        Element collectionProp = element.addElement(COLLECTION_PROP);
        collectionProp.addAttribute("name", "Arguments.arguments");

        // 空的 hashTree
        hashTree.addElement(HASH_TREE_ELEMENT);
    }

    private void processCheckoutConfigTestElement(Element ele) {
        if (context.getProperty("timeout") == null || StringUtils.isBlank(context.getProperty("timeout").toString())) {
            return;
        }

        Element hashTree = getNextSibling(ele);

        List<Element> childNodes = hashTree.elements();
        for (Element item : childNodes) {
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

        Element element = hashTree.addElement(CONFIG_TEST_ELEMENT);
        element.addAttribute("guiclass", "HttpDefaultsGui");
        element.addAttribute("testclass", "ConfigTestElement");
        element.addAttribute("testname", "HTTP Request Defaults");
        element.addAttribute("enabled", "true");

        Element elementProp = element.addElement(ELEMENT_PROP);
        elementProp.addAttribute("name", "HTTPsampler.Arguments");
        elementProp.addAttribute("elementType", "Arguments");
        elementProp.addAttribute("guiclass", "HTTPArgumentsPanel");
        elementProp.addAttribute("testclass", "Arguments");
        elementProp.addAttribute("enabled", "true");

        Element collectionProp = elementProp.addElement(COLLECTION_PROP);
        collectionProp.addAttribute("name", "Arguments.arguments");


        appendStringProp(element, "HTTPSampler.domain", "");
        appendStringProp(element, "HTTPSampler.port", "");
        appendStringProp(element, "HTTPSampler.protocol", "");
        appendStringProp(element, "HTTPSampler.contentEncoding", "");
        appendStringProp(element, "HTTPSampler.path", "");
        appendStringProp(element, "HTTPSampler.concurrentPool", "6");
        appendStringProp(element, "HTTPSampler.connect_timeout", "60000");
        appendStringProp(element, "HTTPSampler.response_timeout", "");

        // 空的 hashTree
        hashTree.addElement(HASH_TREE_ELEMENT);
    }

    private Element getNextSibling(Element ele) {
        Element parent = ele.getParent();
        if (parent != null) {
            Iterator<Element> iterator = parent.elementIterator();
            while (iterator.hasNext()) {
                Element next = iterator.next();
                if (ele.equals(next)) {
                    return iterator.next();
                }
            }
        }
        return null;
    }

    private void processArguments(Element ele) {
        List<Element> childNodes = ele.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, "collectionProp")) {
                //
                Object params = context.getProperty("params");
                if (params instanceof List) {
                    for (Object p : (List) params) {
                        JSONObject jsonObject = JSON.parseObject(p.toString());
                        if (!jsonObject.getBooleanValue("enable")) {
                            continue;
                        }
                        Element elementProp = item.addElement(ELEMENT_PROP);
                        elementProp.addAttribute("name", jsonObject.getString("name"));
                        elementProp.addAttribute("elementType", "Argument");
                        appendStringProp(elementProp, "Argument.name", jsonObject.getString("name"));
                        // 处理 mock data
                        String value = jsonObject.getString("value");
                        appendStringProp(elementProp, "Argument.value", ScriptEngineUtils.buildFunctionCallString(value));
                        appendStringProp(elementProp, "Argument.metadata", "=");
                    }
                }
            }
        }

    }

    private void processConfigTestElement(Element ele) {

        List<Element> childNodes = ele.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, STRING_PROP)
                    && StringUtils.equals(item.attributeValue("name"), "HTTPSampler.connect_timeout")) {
                if (context.getProperty("timeout") != null) {
                    removeChildren(item);
                    item.setText(context.getProperty("timeout").toString());
                }
            }
            // 增加一个response_timeout，避免目标网站不反回结果导致测试不能结束
            if (nodeNameEquals(item, STRING_PROP)
                    && StringUtils.equals(item.attributeValue("name"), "HTTPSampler.response_timeout")) {
                if (context.getProperty("responseTimeout") != null) {
                    removeChildren(item);
                    item.setText(context.getProperty("responseTimeout").toString());
                }
            }
        }
    }

    private void appendBoolProp(Element ele, String name, boolean value) {
        Element boolProp = ele.addElement(BOOL_PROP);
        boolProp.addAttribute("name", name);
        boolProp.setText(String.valueOf(value));
    }

    private void appendIntProp(Element ele, String name, int value) {
        Element intProp = ele.addElement(BOOL_PROP);
        intProp.addAttribute("name", name);
        intProp.setText(String.valueOf(value));
    }

    private void processBackendListener(Element backendListener) {
        String resourcePoolId = context.getResourcePoolId();
        TestResourcePool resourcePool = CommonBeanFactory.getBean(TestResourcePoolService.class).getResourcePool(resourcePoolId);
        if (checkLicense() && !BooleanUtils.toBoolean(resourcePool.getBackendListener())) {
            return;
        }
        KafkaProperties kafkaProperties = CommonBeanFactory.getBean(KafkaProperties.class);
        // 清空child
        removeChildren(backendListener);
        appendStringProp(backendListener, "classname", "io.github.rahulsinghai.jmeter.backendlistener.kafka.KafkaBackendClient");
        appendStringProp(backendListener, "QUEUE_SIZE", kafkaProperties.getQueueSize());
        // elementProp
        Element elementProp = backendListener.addElement(ELEMENT_PROP);
        elementProp.addAttribute("name", "arguments");
        elementProp.addAttribute("elementType", "Arguments");
        elementProp.addAttribute("guiclass", "ArgumentsPanel");
        elementProp.addAttribute("testclass", "Arguments");
        elementProp.addAttribute("enabled", "true");
        Element collectionProp = elementProp.addElement("collectionProp");
        collectionProp.addAttribute("name", "Arguments.arguments");
        appendKafkaProp(collectionProp, "kafka.acks", kafkaProperties.getAcks());
        appendKafkaProp(collectionProp, "kafka.bootstrap.servers", kafkaProperties.getBootstrapServers());
        appendKafkaProp(collectionProp, "kafka.topic", kafkaProperties.getTopic());
        appendKafkaProp(collectionProp, "kafka.sample.filter", kafkaProperties.getSampleFilter());
        appendKafkaProp(collectionProp, "kafka.fields", kafkaProperties.getFields());
        appendKafkaProp(collectionProp, "kafka.test.mode", kafkaProperties.getTestMode());
        appendKafkaProp(collectionProp, "kafka.parse.all.req.headers", kafkaProperties.getParseAllReqHeaders());
        appendKafkaProp(collectionProp, "kafka.parse.all.res.headers", kafkaProperties.getParseAllResHeaders());
        appendKafkaProp(collectionProp, "kafka.timestamp", kafkaProperties.getTimestamp());
        appendKafkaProp(collectionProp, "kafka.compression.type", kafkaProperties.getCompressionType());
        appendKafkaProp(collectionProp, "kafka.ssl.enabled", kafkaProperties.getSsl().getEnabled());
        appendKafkaProp(collectionProp, "kafka.ssl.key.password", kafkaProperties.getSsl().getKeyPassword());
        appendKafkaProp(collectionProp, "kafka.ssl.keystore.location", kafkaProperties.getSsl().getKeystoreLocation());
        appendKafkaProp(collectionProp, "kafka.ssl.keystore.password", kafkaProperties.getSsl().getKeystorePassword());
        appendKafkaProp(collectionProp, "kafka.ssl.truststore.location", kafkaProperties.getSsl().getTruststoreLocation());
        appendKafkaProp(collectionProp, "kafka.ssl.truststore.password", kafkaProperties.getSsl().getTruststorePassword());
        appendKafkaProp(collectionProp, "kafka.ssl.enabled.protocols", kafkaProperties.getSsl().getEnabledProtocols());
        appendKafkaProp(collectionProp, "kafka.ssl.keystore.type", kafkaProperties.getSsl().getKeystoreType());
        appendKafkaProp(collectionProp, "kafka.ssl.protocol", kafkaProperties.getSsl().getProtocol());
        appendKafkaProp(collectionProp, "kafka.ssl.provider", kafkaProperties.getSsl().getProvider());
        appendKafkaProp(collectionProp, "kafka.ssl.truststore.type", kafkaProperties.getSsl().getTruststoreType());
        appendKafkaProp(collectionProp, "kafka.batch.size", kafkaProperties.getBatchSize());
        appendKafkaProp(collectionProp, "kafka.client.id", kafkaProperties.getClientId());
        appendKafkaProp(collectionProp, "kafka.connections.max.idle.ms", kafkaProperties.getConnectionsMaxIdleMs());
        // 添加关联关系 test.id test.name test.startTime test.reportId
        appendKafkaProp(collectionProp, "test.id", context.getTestId());
        appendKafkaProp(collectionProp, "test.name", context.getTestName());
        appendKafkaProp(collectionProp, "test.reportId", context.getReportId());

    }

    private void appendKafkaProp(Element ele, String name, String value) {
        Element eleProp = ele.addElement(ELEMENT_PROP);
        eleProp.addAttribute("name", name);
        eleProp.addAttribute("elementType", "Argument");
        appendStringProp(eleProp, "Argument.name", name);
        appendStringProp(eleProp, "Argument.value", value);
        appendStringProp(eleProp, "Argument.metadata", "=");
    }

    private void processCheckoutBackendListener(Element element) {
        String resourcePoolId = context.getResourcePoolId();
        TestResourcePool resourcePool = CommonBeanFactory.getBean(TestResourcePoolService.class).getResourcePool(resourcePoolId);
        if (checkLicense() && !BooleanUtils.toBoolean(resourcePool.getBackendListener())) {
            return;
        }
        // 已经添加过不再重复添加
        if (context.isCheckBackendListener()) {
            return;
        }

        Element listenerParent = getNextSibling(element);

        List<Element> childNodes = listenerParent.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, BACKEND_LISTENER)) {
                // 如果已经存在，不再添加
                removeChildren(item);
                return;
            }
        }

        // add class name
        Element backendListener = listenerParent.addElement(BACKEND_LISTENER);
        backendListener.addAttribute("guiclass", "BackendListenerGui");
        backendListener.addAttribute("testclass", "BackendListener");
        backendListener.addAttribute("testname", "Backend Listener");
        backendListener.addAttribute("enabled", "true");

        listenerParent.addElement(HASH_TREE_ELEMENT);
        // 标记已经添加上
        context.setCheckBackendListener(true);
    }

    private boolean checkLicense() {
        try {
            ClassUtils.getClass("io.metersphere.xpack.license.service.LicenseService");
            Object licenseService = CommonBeanFactory.getBean("licenseService");
            Object result = MethodUtils.invokeMethod(licenseService, "valid");
            Object status = MethodUtils.invokeMethod(result, "getStatus");
            if (StringUtils.equalsIgnoreCase("VALID", status.toString())) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void processThreadGroup(Element threadGroup) {
        // 检查 threadgroup 后面的hashtree是否为空
        Element hashTree = getNextSibling(threadGroup);
        if (hashTree == null) {
            MSException.throwException(Translator.get("jmx_content_valid"));
        }
        Object tgTypes = context.getProperty("tgType");
        String tgType = "ThreadGroup";
        if (tgTypes instanceof List) {
            Object o = ((List<?>) tgTypes).get(0);
            ((List<?>) tgTypes).remove(0);
            tgType = o.toString();
        }
        if (StringUtils.equals(tgType, THREAD_GROUP)) {
            processBaseThreadGroup(threadGroup, THREAD_GROUP);
        }
        if (StringUtils.equals(tgType, SETUP_THREAD_GROUP)) {
            processBaseThreadGroup(threadGroup, SETUP_THREAD_GROUP);
        }
        if (StringUtils.equals(tgType, POST_THREAD_GROUP)) {
            processBaseThreadGroup(threadGroup, POST_THREAD_GROUP);
        }
        if (StringUtils.equals(tgType, CONCURRENCY_THREAD_GROUP)) {
            processConcurrencyThreadGroup(threadGroup);
        }

    }

    private void processBaseThreadGroup(Element threadGroup, String tgType) {
        threadGroup.setName(tgType);
        threadGroup.addAttribute("guiclass", tgType + "Gui");
        threadGroup.addAttribute("testclass", tgType);
        /*
        <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="登录" enabled="true">
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
      </ThreadGroup>
         */
        removeChildren(threadGroup);
        // 避免出现配置错位
        Object iterateNum = context.getProperty("iterateNum");
        if (iterateNum instanceof List) {
            ((List<?>) iterateNum).remove(0);
        }
        Object iterateRampUpTimes = context.getProperty("iterateRampUpTime");
        if (iterateRampUpTimes instanceof List) {
            ((List<?>) iterateRampUpTimes).remove(0);
        }
        Object steps = context.getProperty("Steps");
        if (steps instanceof List) {
            ((List<?>) steps).remove(0);
        }
        Object holds = context.getProperty("Hold");
        if (holds instanceof List) {
            ((List<?>) holds).remove(0);
        }
        Object targetLevels = context.getProperty("TargetLevel");
        String threads = "10";
        if (targetLevels instanceof List) {
            Object o = ((List<?>) targetLevels).get(0);
            ((List<?>) targetLevels).remove(0);
            threads = o.toString();
        }
        Object rampUps = context.getProperty("RampUp");
        String rampUp = "1";
        if (rampUps instanceof List) {
            Object o = ((List<?>) rampUps).get(0);
            ((List<?>) rampUps).remove(0);
            rampUp = o.toString();
        }
        Object durations = context.getProperty("duration");
        String duration = "2";
        if (durations instanceof List) {
            Object o = ((List<?>) durations).get(0);
            ((List<?>) durations).remove(0);
            duration = o.toString();
        }
        Object onSampleErrors = context.getProperty("onSampleError");
        String onSampleError = "continue";
        if (onSampleErrors instanceof List) {
            Object o = ((List<?>) onSampleErrors).get(0);
            ((List<?>) onSampleErrors).remove(0);
            onSampleError = o.toString();
        }
        Object units = context.getProperty("unit");
        if (units instanceof List) {
            Object o = ((List<?>) units).get(0);
            ((List<?>) units).remove(0);
        }
        Object deleteds = context.getProperty("deleted");
        String deleted = "false";
        if (deleteds instanceof List) {
            Object o = ((List<?>) deleteds).get(0);
            ((List<?>) deleteds).remove(0);
            deleted = o.toString();
        }
        Object enableds = context.getProperty("enabled");
        String enabled = "true";
        if (enableds instanceof List) {
            Object o = ((List<?>) enableds).get(0);
            ((List<?>) enableds).remove(0);
            enabled = o.toString();
        }

        threadGroup.addAttribute("enabled", enabled);
        if (BooleanUtils.toBoolean(deleted)) {
            threadGroup.addAttribute("enabled", "false");
        }
        Element elementProp = threadGroup.addElement(ELEMENT_PROP);
        elementProp.addAttribute("name", "ThreadGroup.main_controller");
        elementProp.addAttribute("elementType", "LoopController");
        elementProp.addAttribute("guiclass", "LoopControlPanel");
        elementProp.addAttribute("testclass", "LoopController");
        elementProp.addAttribute("testname", "Loop Controller");
        appendBoolProp(elementProp, "LoopController.continue_forever", false);
        appendStringProp(elementProp, "LoopController.loops", "-1");
        //
        appendStringProp(threadGroup, "ThreadGroup.on_sample_error", onSampleError);
        appendStringProp(threadGroup, "ThreadGroup.num_threads", threads);
        appendStringProp(threadGroup, "ThreadGroup.ramp_time", rampUp);
        appendStringProp(threadGroup, "ThreadGroup.duration", duration);
        appendStringProp(threadGroup, "ThreadGroup.delay", "0");
        appendBoolProp(threadGroup, "ThreadGroup.scheduler", true);
        appendBoolProp(threadGroup, "ThreadGroup.same_user_on_next_iteration", true);
    }

    private void processConcurrencyThreadGroup(Element threadGroup) {
        // 重命名 tagName
        threadGroup.setName(CONCURRENCY_THREAD_GROUP);
        threadGroup.addAttribute("guiclass", CONCURRENCY_THREAD_GROUP + "Gui");
        threadGroup.addAttribute("testclass", CONCURRENCY_THREAD_GROUP);
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
        // 避免出现配置错位
        Object iterateNum = context.getProperty("iterateNum");
        if (iterateNum instanceof List) {
            ((List<?>) iterateNum).remove(0);
        }
        Object iterateRampUpTimes = context.getProperty("iterateRampUpTime");
        if (iterateRampUpTimes instanceof List) {
            ((List<?>) iterateRampUpTimes).remove(0);
        }
        Object durations = context.getProperty("duration");
        if (durations instanceof List) {
            ((List<?>) durations).remove(0);
        }
        // elementProp
        Object targetLevels = context.getProperty("TargetLevel");
        String threads = "10";
        if (targetLevels instanceof List) {
            Object o = ((List<?>) targetLevels).get(0);
            ((List<?>) targetLevels).remove(0);
            threads = o.toString();
        }
        Object rampUps = context.getProperty("RampUp");
        String rampUp = "1";
        if (rampUps instanceof List) {
            Object o = ((List<?>) rampUps).get(0);
            ((List<?>) rampUps).remove(0);
            rampUp = o.toString();
        }
        Object steps = context.getProperty("Steps");
        String step = "2";
        if (steps instanceof List) {
            Object o = ((List<?>) steps).get(0);
            ((List<?>) steps).remove(0);
            step = o.toString();
        }
        Object holds = context.getProperty("Hold");
        String hold = "2";
        if (holds instanceof List) {
            Object o = ((List<?>) holds).get(0);
            ((List<?>) holds).remove(0);
            hold = o.toString();
        }
        Object onSampleErrors = context.getProperty("onSampleError");
        String onSampleError = "continue";
        if (onSampleErrors instanceof List) {
            Object o = ((List<?>) onSampleErrors).get(0);
            ((List<?>) onSampleErrors).remove(0);
            onSampleError = o.toString();
        }
        Object units = context.getProperty("unit");
        if (units instanceof List) {
            Object o = ((List<?>) units).get(0);
            ((List<?>) units).remove(0);
        }
        Object deleteds = context.getProperty("deleted");
        String deleted = "false";
        if (deleteds instanceof List) {
            Object o = ((List<?>) deleteds).get(0);
            ((List<?>) deleteds).remove(0);
            deleted = o.toString();
        }
        Object enableds = context.getProperty("enabled");
        String enabled = "true";
        if (enableds instanceof List) {
            Object o = ((List<?>) enableds).get(0);
            ((List<?>) enableds).remove(0);
            enabled = o.toString();
        }

        threadGroup.addAttribute("enabled", enabled);
        if (BooleanUtils.toBoolean(deleted)) {
            threadGroup.addAttribute("enabled", "false");
        }
        Element elementProp = threadGroup.addElement(ELEMENT_PROP);
        elementProp.addAttribute("name", "ThreadGroup.main_controller");
        elementProp.addAttribute("elementType", "com.blazemeter.jmeter.control.VirtualUserController");

        appendStringProp(threadGroup, "ThreadGroup.on_sample_error", onSampleError);
        appendStringProp(threadGroup, "TargetLevel", threads);
        appendStringProp(threadGroup, "RampUp", rampUp);
        appendStringProp(threadGroup, "Steps", step);
        appendStringProp(threadGroup, "Hold", hold);
        appendStringProp(threadGroup, "LogFilename", "");
        // bzm - Concurrency Thread Group "Thread Iterations Limit:" 设置为空
//        threadGroup.appendChild(createStringProp(document, "Iterations", "1"));
        appendStringProp(threadGroup, "Unit", "S");
    }

    private void processIterationThreadGroup(Element threadGroup) {
        // 检查 threadgroup 后面的hashtree是否为空
        Element hashTree = getNextSibling(threadGroup);
        if (hashTree == null) {
            MSException.throwException(Translator.get("jmx_content_valid"));
        }
        Object tgTypes = context.getProperty("tgType");
        String tgType = "ThreadGroup";
        if (tgTypes instanceof List) {
            Object o = ((List<?>) tgTypes).get(0);
            ((List<?>) tgTypes).remove(0);
            tgType = o.toString();
        }
        if (StringUtils.equals(tgType, THREAD_GROUP)) {
            threadGroup.setName(THREAD_GROUP);
            threadGroup.addAttribute("guiclass", THREAD_GROUP + "Gui");
            threadGroup.addAttribute("testclass", THREAD_GROUP);
        }
        if (StringUtils.equals(tgType, SETUP_THREAD_GROUP)) {
            threadGroup.setName(SETUP_THREAD_GROUP);
            threadGroup.addAttribute("guiclass", SETUP_THREAD_GROUP + "Gui");
            threadGroup.addAttribute("testclass", SETUP_THREAD_GROUP);
        }
        if (StringUtils.equals(tgType, POST_THREAD_GROUP)) {
            threadGroup.setName(POST_THREAD_GROUP);
            threadGroup.addAttribute("guiclass", POST_THREAD_GROUP + "Gui");
            threadGroup.addAttribute("testclass", POST_THREAD_GROUP);
        }
        removeChildren(threadGroup);

        // 选择按照迭代次数处理线程组
        /*
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">1</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">5</stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">10</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
         */
        // elementProp
        // 避免出现配置错位
        Object durations = context.getProperty("duration");
        if (durations instanceof List) {
            ((List<?>) durations).remove(0);
        }
        Object units = context.getProperty("unit");
        if (units instanceof List) {
            ((List<?>) units).remove(0);
        }
        Object holds = context.getProperty("Hold");
        if (holds instanceof List) {
            ((List<?>) holds).remove(0);
        }
        Object steps = context.getProperty("Steps");
        if (steps instanceof List) {
            ((List<?>) steps).remove(0);
        }
        Object arampUps = context.getProperty("RampUp");
        if (arampUps instanceof List) {
            ((List<?>) arampUps).remove(0);
        }
        Object targetLevels = context.getProperty("TargetLevel");
        String threads = "10";
        if (targetLevels instanceof List) {
            Object o = ((List<?>) targetLevels).get(0);
            ((List<?>) targetLevels).remove(0);
            threads = o.toString();
        }
        Object iterateNum = context.getProperty("iterateNum");
        String loops = "1";
        if (iterateNum instanceof List) {
            Object o = ((List<?>) iterateNum).get(0);
            ((List<?>) iterateNum).remove(0);
            loops = o.toString();
        }
        Object onSampleErrors = context.getProperty("onSampleError");
        String onSampleError = "continue";
        if (onSampleErrors instanceof List) {
            Object o = ((List<?>) onSampleErrors).get(0);
            ((List<?>) onSampleErrors).remove(0);
            onSampleError = o.toString();
        }
        Object rampUps = context.getProperty("iterateRampUpTime");
        String rampUp = "10";
        if (rampUps instanceof List) {
            Object o = ((List<?>) rampUps).get(0);
            ((List<?>) rampUps).remove(0);
            rampUp = o.toString();
        }
        Object deleteds = context.getProperty("deleted");
        String deleted = "false";
        if (deleteds instanceof List) {
            Object o = ((List<?>) deleteds).get(0);
            ((List<?>) deleteds).remove(0);
            deleted = o.toString();
        }
        Object enableds = context.getProperty("enabled");
        String enabled = "true";
        if (enableds instanceof List) {
            Object o = ((List<?>) enableds).get(0);
            ((List<?>) enableds).remove(0);
            enabled = o.toString();
        }
        threadGroup.addAttribute("enabled", enabled);
        if (BooleanUtils.toBoolean(deleted)) {
            threadGroup.addAttribute("enabled", "false");
        }
        Element elementProp = threadGroup.addElement(ELEMENT_PROP);
        elementProp.addAttribute("name", "ThreadGroup.main_controller");
        elementProp.addAttribute("elementType", "LoopController");
        elementProp.addAttribute("guiclass", "LoopControlPanel");
        elementProp.addAttribute("testclass", "LoopController");
        elementProp.addAttribute("testname", "Loop Controller");
        elementProp.addAttribute("enabled", "true");
        appendBoolProp(elementProp, "LoopController.continue_forever", false);
        appendStringProp(elementProp, "LoopController.loops", loops);

        appendStringProp(threadGroup, "ThreadGroup.on_sample_error", onSampleError);
        appendStringProp(threadGroup, "ThreadGroup.num_threads", threads);
        appendStringProp(threadGroup, "ThreadGroup.ramp_time", rampUp);
        appendBoolProp(threadGroup, "ThreadGroup.scheduler", false);
        appendStringProp(threadGroup, "Hold", "1");
        appendStringProp(threadGroup, "ThreadGroup.duration", "10");
        appendStringProp(threadGroup, "ThreadGroup.delay", "");
        appendBoolProp(threadGroup, "ThreadGroup.same_user_on_next_iteration", true);
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
        if (context.getProperty("rpsLimitEnable") == null) {
            return;
        }
        Object rpsLimitEnables = context.getProperty("rpsLimitEnable");
        if (rpsLimitEnables instanceof List) {
            Object o = ((List<?>) rpsLimitEnables).get(0);
            ((List<?>) rpsLimitEnables).remove(0);
            if (o == null || "false".equals(o.toString())) {
                return;
            }
        }

        Element timerParent = getNextSibling(element);

        List<Element> childNodes = timerParent.elements();
        for (Element item : childNodes) {
            if (nodeNameEquals(item, VARIABLE_THROUGHPUT_TIMER)) {
                // 如果已经存在，不再添加
                return;
            }
        }

        Element timer = timerParent.addElement(VARIABLE_THROUGHPUT_TIMER);
        timer.addAttribute("guiclass", VARIABLE_THROUGHPUT_TIMER + "Gui");
        timer.addAttribute("testclass", VARIABLE_THROUGHPUT_TIMER);
        timer.addAttribute("testname", "jp@gc - Throughput Shaping Timer");
        timer.addAttribute("enabled", "true");

        Element collectionProp = timer.addElement("collectionProp");
        collectionProp.addAttribute("name", "load_profile");
        Element childCollectionProp = collectionProp.addElement("collectionProp");
        childCollectionProp.addAttribute("name", "140409499");
        appendStringProp(childCollectionProp, "49", "1");
        appendStringProp(childCollectionProp, "49", "1");
        appendStringProp(childCollectionProp, "1570", "10");
        // 添加一个空的hashTree
        timerParent.addElement(HASH_TREE_ELEMENT);
    }

    private void appendStringProp(Element element, String name, String value) {
        Element ele = element.addElement(STRING_PROP);
        ele.addAttribute("name", name);
        ele.setText(value);
    }

    private void processThreadGroupName(Element threadGroup) {
        String testname = threadGroup.attributeValue("testname");
        threadGroup.addAttribute("testname", testname + "-" + context.getResourceIndex());
    }

    private void processVariableThroughputTimer(Element variableThroughputTimer) {
        // 设置rps时长
        Integer duration = Integer.MAX_VALUE;
        Object rpsLimits = context.getProperty("rpsLimit");
        String rpsLimit;
        if (rpsLimits instanceof List) {
            Object o = ((List<?>) rpsLimits).get(0);
            ((List<?>) rpsLimits).remove(0);
            rpsLimit = o.toString();
        } else {
            rpsLimit = rpsLimits.toString();
        }
        if (variableThroughputTimer.elements().size() > 0) {
            List<Element> childNodes = variableThroughputTimer.elements();
            for (Element ele : childNodes) {
                if (ele != null) {
                    if (invalid(ele)) {
                        continue;
                    }

                    // TODO kg.apc.jmeter.timers.VariableThroughputTimer的stringProp的name属性是动态的
                    if (nodeNameEquals(ele, COLLECTION_PROP)) {
                        List<Element> eleChildNodes = ele.elements();
                        for (Element item : eleChildNodes) {
                            if (nodeNameEquals(item, COLLECTION_PROP)) {
                                int stringPropCount = 0;
                                List<Element> itemChildNodes = item.elements();
                                for (Element prop : itemChildNodes) {
                                    if (nodeNameEquals(prop, STRING_PROP)) {
                                        if (stringPropCount < 2) {
                                            stringPropCount++;
                                        } else {
                                            stringPropCount = 0;
                                            prop.setText(String.valueOf(duration));
                                            continue;
                                        }
                                        prop.setText(rpsLimit);
                                    }
                                }
                            }

                        }
                    }

                }
            }
        }
    }

    private boolean nodeNameEquals(Element element, String desiredName) {
        return desiredName.equals(element.getName());
    }

    private boolean invalid(Element ele) {
        return !StringUtils.isBlank(ele.attributeValue("enabled")) && !Boolean.parseBoolean(ele.attributeValue("enabled"));
    }

    private void removeChildren(Element node) {
        List<Element> elements = node.elements();
        for (Element ele : elements) {
            node.remove(ele);
        }
    }
}
