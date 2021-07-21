package io.metersphere.performance.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.Application;
import io.metersphere.api.dto.RunRequest;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.performance.engine.docker.DockerTestEngine;
import io.metersphere.performance.parse.EngineSourceParser;
import io.metersphere.performance.parse.EngineSourceParserFactory;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.FileService;
import io.metersphere.service.KubernetesTestEngine;
import io.metersphere.service.TestResourcePoolService;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections8.Reflections;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EngineFactory {
    private static FileService fileService;
    private static PerformanceTestService performanceTestService;
    private static TestResourcePoolService testResourcePoolService;
    private static Class<? extends KubernetesTestEngine> kubernetesTestEngineClass;

    static {
        Reflections reflections = new Reflections(Application.class.getPackage().getName());
        Set<Class<? extends KubernetesTestEngine>> implClass = reflections.getSubTypesOf(KubernetesTestEngine.class);
        for (Class<? extends KubernetesTestEngine> aClass : implClass) {
            kubernetesTestEngineClass = aClass;
            // 第一个
            break;
        }
    }

    public static Engine createEngine(LoadTestWithBLOBs loadTest) {
        String resourcePoolId = loadTest.getTestResourcePoolId();
        if (StringUtils.isBlank(resourcePoolId)) {
            MSException.throwException(Translator.get("test_resource_pool_id_is_null"));
        }

        TestResourcePool resourcePool = testResourcePoolService.getResourcePool(resourcePoolId);
        if (resourcePool == null) {
            MSException.throwException(Translator.get("test_resource_pool_id_is_null"));
        }

        final ResourcePoolTypeEnum type = ResourcePoolTypeEnum.valueOf(resourcePool.getType());

        if (type == ResourcePoolTypeEnum.NODE) {
            return new DockerTestEngine(loadTest);
        }
        if (type == ResourcePoolTypeEnum.K8S) {
            try {
                return (Engine) ConstructorUtils.invokeConstructor(kubernetesTestEngineClass, loadTest);
            } catch (Exception e) {
                LogUtil.error(e);
                return null;
            }
        }
        return null;
    }

    public static Engine createApiEngine(RunRequest runRequest) {
        try {
            return (Engine) ConstructorUtils.invokeConstructor(kubernetesTestEngineClass, runRequest);
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e.getMessage());
        }
        return null;
    }

    public static EngineContext createContext(LoadTestWithBLOBs loadTest, double[] ratios, String reportId, int resourceIndex) {
        final List<FileMetadata> fileMetadataList = performanceTestService.getFileMetadataByTestId(loadTest.getId());
        if (org.springframework.util.CollectionUtils.isEmpty(fileMetadataList)) {
            MSException.throwException(Translator.get("run_load_test_file_not_found") + loadTest.getId());
        }

        List<FileMetadata> jmxFiles = fileMetadataList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getType(), FileType.JMX.name())).collect(Collectors.toList());
        List<FileMetadata> resourceFiles = ListUtils.subtract(fileMetadataList, jmxFiles);
        // 合并上传的jmx
        byte[] jmxBytes = mergeJmx(jmxFiles);
        final EngineContext engineContext = new EngineContext();
        engineContext.setTestId(loadTest.getId());
        engineContext.setTestName(loadTest.getName());
        engineContext.setNamespace(loadTest.getProjectId());
        engineContext.setFileType(FileType.JMX.name());
        engineContext.setResourcePoolId(loadTest.getTestResourcePoolId());
        engineContext.setReportId(reportId);
        engineContext.setResourceIndex(resourceIndex);
        engineContext.setRatios(ratios);

        if (StringUtils.isNotEmpty(loadTest.getLoadConfiguration())) {
            final JSONArray jsonArray = JSONObject.parseArray(loadTest.getLoadConfiguration());

            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i) instanceof List) {
                    JSONArray o = jsonArray.getJSONArray(i);
                    for (int j = 0; j < o.size(); j++) {
                        JSONObject b = o.getJSONObject(j);
                        String key = b.getString("key");
                        Object values = engineContext.getProperty(key);
                        if (values == null) {
                            values = new ArrayList<>();
                        }
                        if (values instanceof List) {
                            Object value = b.get("value");
                            if ("TargetLevel".equals(key)) {
                                Integer targetLevel = ((Integer) b.get("value"));
                                if (resourceIndex + 1 == ratios.length) {
                                    double beforeLast = 0; // 前几个线程数
                                    for (int k = 0; k < ratios.length - 1; k++) {
                                        beforeLast += Math.round(targetLevel * ratios[k]);
                                    }
                                    value = Math.round(targetLevel - beforeLast);
                                } else {
                                    value = Math.round(targetLevel * ratios[resourceIndex]);
                                }
                            }
                            ((List<Object>) values).add(value);
                            engineContext.addProperty(key, values);
                        }
                    }
                }
            }
        }
        /*
        {"timeout":10,"statusCode":["302","301"],"params":[{"name":"param1","enable":true,"value":"0","edit":false}],"domains":[{"domain":"baidu.com","enable":true,"ip":"127.0.0.1","edit":false}]}
         */
        Map<String, byte[]> testResourceFiles = new HashMap<>();
        StringBuilder props = new StringBuilder("# JMeter Properties\n");
        if (StringUtils.isNotEmpty(loadTest.getAdvancedConfiguration())) {
            JSONObject advancedConfiguration = JSONObject.parseObject(loadTest.getAdvancedConfiguration());
            engineContext.addProperties(advancedConfiguration);
            JSONArray properties = advancedConfiguration.getJSONArray("properties");
            if (properties != null) {
                for (int i = 0; i < properties.size(); i++) {
                    JSONObject prop = properties.getJSONObject(i);
                    if (!prop.getBoolean("enable")) {
                        continue;
                    }
                    props.append(prop.getString("name")).append("=").append(prop.getString("value")).append("\n");
                }
            }
        }
        // JMeter Properties
        testResourceFiles.put("ms.properties", props.toString().getBytes(StandardCharsets.UTF_8));

        final EngineSourceParser engineSourceParser = EngineSourceParserFactory.createEngineSourceParser(engineContext.getFileType());

        if (engineSourceParser == null) {
            MSException.throwException("File type unknown");
        }

        if (CollectionUtils.isNotEmpty(resourceFiles)) {
            resourceFiles.forEach(cf -> {
                FileContent csvContent = fileService.getFileContent(cf.getId());
                testResourceFiles.put(cf.getName(), csvContent.getFile());
            });
        }
        engineContext.setTestResourceFiles(testResourceFiles);

        try (ByteArrayInputStream source = new ByteArrayInputStream(jmxBytes)) {
            String content = engineSourceParser.parse(engineContext, source);
            engineContext.setContent(content);
        } catch (MSException e) {
            LogUtil.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }

        return engineContext;
    }

    public static byte[] mergeJmx(List<FileMetadata> jmxFiles) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Element hashTree = null;
            Document rootDocument = null;
            for (FileMetadata fileMetadata : jmxFiles) {
                FileContent fileContent = fileService.getFileContent(fileMetadata.getId());
                final InputSource inputSource = new InputSource(new ByteArrayInputStream(fileContent.getFile()));
                if (hashTree == null) {
                    rootDocument = docBuilder.parse(inputSource);
                    Element jmeterTestPlan = rootDocument.getDocumentElement();
                    NodeList childNodes = jmeterTestPlan.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node node = childNodes.item(i);
                        if (node instanceof Element) {
                            // jmeterTestPlan的子元素肯定是<hashTree></hashTree>
                            hashTree = (Element) node;
                            break;
                        }
                    }
                } else {
                    Document document = docBuilder.parse(inputSource);
                    Element jmeterTestPlan = document.getDocumentElement();
                    NodeList childNodes = jmeterTestPlan.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node node = childNodes.item(i);
                        if (node instanceof Element) {
                            // jmeterTestPlan的子元素肯定是<hashTree></hashTree>
                            Element secondHashTree = (Element) node;
                            NodeList secondChildNodes = secondHashTree.getChildNodes();
                            for (int j = 0; j < secondChildNodes.getLength(); j++) {
                                Node item = secondChildNodes.item(j);
                                Node newNode = item.cloneNode(true);
                                rootDocument.adoptNode(newNode);
                                hashTree.appendChild(newNode);
                            }
                        }
                    }
                }
            }
            return documentToBytes(rootDocument);
        } catch (Exception e) {
            MSException.throwException(e);
        }
        return new byte[0];
    }

    private static byte[] documentToBytes(Document document) throws TransformerException {
        DOMSource domSource = new DOMSource(document);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(out);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return out.toByteArray();
    }

    @Resource
    private void setFileService(FileService fileService) {
        EngineFactory.fileService = fileService;
    }

    @Resource
    public void setTestResourcePoolService(TestResourcePoolService testResourcePoolService) {
        EngineFactory.testResourcePoolService = testResourcePoolService;
    }

    @Resource
    public void setPerformanceTestService(PerformanceTestService performanceTestService) {
        EngineFactory.performanceTestService = performanceTestService;
    }
}
