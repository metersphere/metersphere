package io.metersphere.performance.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.Application;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
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
import org.dom4j.Document;
import org.dom4j.Element;
import org.reflections8.Reflections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
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

    public static Engine createEngine(LoadTestReportWithBLOBs loadTestReport) {
        String resourcePoolId = loadTestReport.getTestResourcePoolId();
        if (StringUtils.isBlank(resourcePoolId)) {
            MSException.throwException(Translator.get("test_resource_pool_id_is_null"));
        }

        TestResourcePool resourcePool = testResourcePoolService.getResourcePool(resourcePoolId);
        if (resourcePool == null) {
            MSException.throwException(Translator.get("test_resource_pool_id_is_null"));
        }
        if (ResourceStatusEnum.INVALID.name().equals(resourcePool.getStatus())) {
            MSException.throwException(Translator.get("test_resource_pool_invalid"));
        }

        final ResourcePoolTypeEnum type = ResourcePoolTypeEnum.valueOf(resourcePool.getType());

        if (type == ResourcePoolTypeEnum.NODE) {
            return new DockerTestEngine(loadTestReport);
        }
        if (type == ResourcePoolTypeEnum.K8S) {
            try {
                return (Engine) ConstructorUtils.invokeConstructor(kubernetesTestEngineClass, loadTestReport);
            } catch (Exception e) {
                LogUtil.error(e);
                return null;
            }
        }
        return null;
    }

    public static Engine createApiEngine(JmeterRunRequestDTO runRequest) {
        try {
            return (Engine) ConstructorUtils.invokeConstructor(kubernetesTestEngineClass, runRequest);
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e.getMessage());
        }
        return null;
    }

    public static EngineContext createContext(LoadTestReportWithBLOBs loadTestReport, double[] ratios, String reportId, int resourceIndex) {
        final List<FileMetadata> fileMetadataList = performanceTestService.getFileMetadataByTestId(loadTestReport.getTestId());
        if (org.springframework.util.CollectionUtils.isEmpty(fileMetadataList)) {
            MSException.throwException(Translator.get("run_load_test_file_not_found") + loadTestReport.getTestId());
        }
        // 报告页面点击下载执行zip
        boolean isLocal = false;
        if (ratios.length == 1 && ratios[0] < 0) {
            ratios[0] = 1;
            isLocal = true;
        }

        List<FileMetadata> jmxFiles = fileMetadataList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getType(), FileType.JMX.name())).collect(Collectors.toList());
        List<FileMetadata> resourceFiles = ListUtils.subtract(fileMetadataList, jmxFiles);
        // 合并上传的jmx
        byte[] jmxBytes = mergeJmx(jmxFiles);
        final EngineContext engineContext = new EngineContext();
        engineContext.setTestId(loadTestReport.getTestId());
        engineContext.setTestName(loadTestReport.getName());
        engineContext.setNamespace(loadTestReport.getProjectId());
        engineContext.setFileType(FileType.JMX.name());
        engineContext.setResourcePoolId(loadTestReport.getTestResourcePoolId());
        engineContext.setReportId(reportId);
        engineContext.setResourceIndex(resourceIndex);
        engineContext.setRatios(ratios);

        if (StringUtils.isNotEmpty(loadTestReport.getLoadConfiguration())) {
            final JSONArray jsonArray = JSONObject.parseArray(loadTestReport.getLoadConfiguration());

            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i) instanceof List) {
                    JSONArray o = jsonArray.getJSONArray(i);
                    String strategy = "auto";
                    int resourceNodeIndex = 0;
                    JSONArray tgRatios = null;
                    for (int j = 0; j < o.size(); j++) {
                        JSONObject b = o.getJSONObject(j);
                        String key = b.getString("key");
                        if ("strategy".equals(key) && !isLocal) {
                            strategy = b.getString("value");
                        }
                        if ("resourceNodeIndex".equals(key)) {
                            resourceNodeIndex = b.getIntValue("value");
                        }
                        if ("ratios".equals(key)) {
                            tgRatios = b.getJSONArray("value");
                        }
                    }
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
                                switch (strategy) {
                                    default:
                                    case "auto":
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
                                        break;
                                    case "specify":
                                        Integer threadNum = ((Integer) b.get("value"));
                                        if (resourceNodeIndex == resourceIndex) {
                                            value = Math.round(threadNum);
                                        } else {
                                            value = Math.round(0);
                                        }
                                        break;
                                    case "custom":
                                        Integer threadNum2 = ((Integer) b.get("value"));
                                        if (CollectionUtils.isNotEmpty(tgRatios)) {
                                            if (resourceIndex + 1 == tgRatios.size()) {
                                                double beforeLast = 0; // 前几个线程数
                                                for (int k = 0; k < tgRatios.size() - 1; k++) {
                                                    beforeLast += Math.round(threadNum2 * tgRatios.getDoubleValue(k));
                                                }
                                                value = Math.round(threadNum2 - beforeLast);
                                            } else {
                                                value = Math.round(threadNum2 * tgRatios.getDoubleValue(resourceIndex));
                                            }
                                        }
                                        break;
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
        byte[] props = getJMeterProperties(loadTestReport, engineContext);
        byte[] sysProps = getSystemProperties(loadTestReport, engineContext);
        byte[] hosts = getDNSConfig(loadTestReport, engineContext);
        // JMeter Properties
        testResourceFiles.put("ms.properties", props);
        // System Properties
        testResourceFiles.put("sys.properties", sysProps);
        // DNS
        testResourceFiles.put("hosts", hosts);

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
            byte[] content = engineSourceParser.parse(engineContext, source);
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

    private static byte[] getDNSConfig(LoadTestReportWithBLOBs loadTestReport, EngineContext engineContext) {
        StringBuilder dns = new StringBuilder("# DNS Config\n");
        if (StringUtils.isNotEmpty(loadTestReport.getAdvancedConfiguration())) {
            JSONObject advancedConfiguration = JSONObject.parseObject(loadTestReport.getAdvancedConfiguration());
            engineContext.addProperties(advancedConfiguration);
            JSONArray domains = advancedConfiguration.getJSONArray("domains");
            if (domains != null) {
                for (int i = 0; i < domains.size(); i++) {
                    JSONObject prop = domains.getJSONObject(i);
                    if (!prop.getBoolean("enable")) {
                        continue;
                    }
                    dns.append(prop.getString("ip")).append(" ").append(prop.getString("domain")).append("\n");
                }
            }
        }
        return dns.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getJMeterProperties(LoadTestReportWithBLOBs loadTestReportWithBLOBs, EngineContext engineContext) {
        StringBuilder props = new StringBuilder("# JMeter Properties\n");
        if (StringUtils.isNotEmpty(loadTestReportWithBLOBs.getAdvancedConfiguration())) {
            JSONObject advancedConfiguration = JSONObject.parseObject(loadTestReportWithBLOBs.getAdvancedConfiguration());
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
        return props.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getSystemProperties(LoadTestReportWithBLOBs loadTestReportWithBLOBs, EngineContext engineContext) {
        StringBuilder props = new StringBuilder("# System Properties\n");
        if (StringUtils.isNotEmpty(loadTestReportWithBLOBs.getAdvancedConfiguration())) {
            JSONObject advancedConfiguration = JSONObject.parseObject(loadTestReportWithBLOBs.getAdvancedConfiguration());
            engineContext.addProperties(advancedConfiguration);
            JSONArray systemProperties = advancedConfiguration.getJSONArray("systemProperties");
            if (systemProperties != null) {
                for (int i = 0; i < systemProperties.size(); i++) {
                    JSONObject prop = systemProperties.getJSONObject(i);
                    if (!prop.getBoolean("enable")) {
                        continue;
                    }
                    props.append(prop.getString("name")).append("=").append(prop.getString("value")).append("\n");
                }
            }
        }
        return props.toString().getBytes(StandardCharsets.UTF_8);
    }


    public static byte[] mergeJmx(List<FileMetadata> jmxFiles) {
        try {
            Element hashTree = null;
            Document rootDocument = null;
            for (FileMetadata fileMetadata : jmxFiles) {
                FileContent fileContent = fileService.getFileContent(fileMetadata.getId());
                InputStream inputSource = new ByteArrayInputStream(fileContent.getFile());
                if (hashTree == null) {
                    rootDocument = EngineSourceParserFactory.getDocument(inputSource);
                    Element jmeterTestPlan = rootDocument.getRootElement();
                    List<Element> childNodes = jmeterTestPlan.elements();

                    outer:
                    for (Element node : childNodes) {
                        // jmeterTestPlan的子元素肯定是<hashTree></hashTree>
                        List<Element> childNodes1 = node.elements();
                        for (Element item : childNodes1) {
                            if (StringUtils.equalsIgnoreCase("TestPlan", item.getName())) {
                                hashTree = getNextSibling(item);
                                break outer;
                            }
                        }
                    }
                } else {
                    Document document = EngineSourceParserFactory.getDocument(inputSource);
                    Element jmeterTestPlan = document.getRootElement();
                    List<Element> childNodes = jmeterTestPlan.elements();
                    for (Element node : childNodes) {
                        // jmeterTestPlan的子元素肯定是<hashTree></hashTree>
                        Element secondHashTree = node;
                        List<Element> secondChildNodes = secondHashTree.elements();
                        for (Element item : secondChildNodes) {
                            if (StringUtils.equalsIgnoreCase("TestPlan", item.getName())) {
                                secondHashTree = getNextSibling(item);
                                break;
                            }
                        }
                        if (StringUtils.equalsIgnoreCase("hashTree", secondHashTree.getName())) {
                            List<Element> itemChildNodes = secondHashTree.elements();
                            for (Element item1 : itemChildNodes) {
                                hashTree.add((Element) item1.clone());
                            }
                        }

                    }
                }
                //
                inputSource.close();
            }
            return EngineSourceParserFactory.getBytes(rootDocument);
        } catch (Exception e) {
            MSException.throwException(e);
        }
        return new byte[0];
    }

    private static Element getNextSibling(Element ele) {
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
