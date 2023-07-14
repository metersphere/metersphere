package io.metersphere.engine;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.parse.EngineSourceParser;
import io.metersphere.parse.EngineSourceParserFactory;
import io.metersphere.service.BaseTestResourcePoolService;
import io.metersphere.service.PerformanceReportService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineFactory {
    private static FileMetadataService fileMetadataService;
    private static PerformanceReportService performanceReportService;
    private static BaseTestResourcePoolService baseTestResourcePoolService;

    public static Engine createEngine(LoadTestReportWithBLOBs loadTestReport) {
        String resourcePoolId = loadTestReport.getTestResourcePoolId();
        if (StringUtils.isBlank(resourcePoolId)) {
            MSException.throwException(Translator.get("test_resource_pool_id_is_null"));
        }

        TestResourcePool resourcePool = baseTestResourcePoolService.getResourcePool(resourcePoolId);
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
            return new KubernetesTestEngine(loadTestReport);
        }
        return null;
    }


    public static EngineContext createContext(LoadTestReportWithBLOBs loadTestReport, double[] ratios, int resourceIndex) {
        final List<FileMetadata> fileMetadataList = performanceReportService.getFileMetadataByReportId(loadTestReport.getId());
        if (CollectionUtils.isEmpty(fileMetadataList)) {
            MSException.throwException(Translator.get("run_load_test_file_not_found") + loadTestReport.getId());
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
        engineContext.setReportId(loadTestReport.getId());
        engineContext.setResourceIndex(resourceIndex);
        engineContext.setRatios(ratios);

        if (StringUtils.isNotEmpty(loadTestReport.getLoadConfiguration())) {
            final List jsonArray = JSON.parseArray(loadTestReport.getLoadConfiguration());

            for (Object item : jsonArray) {
                if (item instanceof List) {
                    List o = (List) item;
                    String strategy = "auto";
                    int resourceNodeIndex = 0;
                    List tgRatios = null;
                    for (Object element : o) {
                        Map b = (Map) element;
                        String key = (String) b.get("key");
                        if ("strategy".equals(key) && !isLocal) {
                            strategy = (String) b.get("value");
                        }
                        if ("resourceNodeIndex".equals(key)) {
                            resourceNodeIndex = (int) b.get("value");
                        }
                        if ("ratios".equals(key)) {
                            tgRatios = (List) b.get("value");
                        }
                    }
                    for (Object element : o) {
                        Map b = (Map) element;
                        String key = (String) b.get("key");
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
                                                    if (tgRatios.get(k) instanceof BigDecimal) {
                                                        beforeLast += Math.round(threadNum2 * ((BigDecimal) tgRatios.get(k)).floatValue());
                                                    } else {
                                                        beforeLast += Math.round(threadNum2 * (double) tgRatios.get(k));
                                                    }
                                                }
                                                value = Math.round(threadNum2 - beforeLast);
                                            } else {
                                                if (tgRatios.get(resourceIndex) instanceof BigDecimal) {
                                                    value = Math.round(threadNum2 * ((BigDecimal) tgRatios.get(resourceIndex)).floatValue());
                                                } else {
                                                    value = Math.round(threadNum2 * (double) tgRatios.get(resourceIndex));
                                                }
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
                byte[] csvContent = fileMetadataService.loadFileAsBytes(cf.getId());
                testResourceFiles.put(cf.getName(), csvContent);
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
            Map advancedConfiguration = JSON.parseObject(loadTestReport.getAdvancedConfiguration(), Map.class);
            engineContext.addProperties(advancedConfiguration);
            List domains = (List) advancedConfiguration.get("domains");
            if (domains != null) {
                for (Object domain : domains) {
                    Map prop = (Map) domain;
                    if (!(Boolean) prop.get("enable")) {
                        continue;
                    }
                    dns.append(prop.get("ip")).append(StringUtils.SPACE).append(prop.get("domain")).append(StringUtils.LF);
                }
            }
        }
        return dns.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getJMeterProperties(LoadTestReportWithBLOBs loadTestReportWithBLOBs, EngineContext engineContext) {
        StringBuilder props = new StringBuilder("# JMeter Properties\n");
        if (StringUtils.isNotEmpty(loadTestReportWithBLOBs.getAdvancedConfiguration())) {
            Map advancedConfiguration = JSON.parseObject(loadTestReportWithBLOBs.getAdvancedConfiguration(), Map.class);
            engineContext.addProperties(advancedConfiguration);
            List properties = (List) advancedConfiguration.get("properties");
            if (properties != null) {
                for (Object property : properties) {
                    Map prop = (Map) property;
                    if (!(Boolean) prop.get("enable")) {
                        continue;
                    }
                    props.append(prop.get("name")).append("=").append(prop.get("value")).append(StringUtils.LF);
                }
            }
        }
        return props.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getSystemProperties(LoadTestReportWithBLOBs loadTestReportWithBLOBs, EngineContext engineContext) {
        StringBuilder props = new StringBuilder("# System Properties\n");
        if (StringUtils.isNotEmpty(loadTestReportWithBLOBs.getAdvancedConfiguration())) {
            Map advancedConfiguration = JSON.parseObject(loadTestReportWithBLOBs.getAdvancedConfiguration(), Map.class);
            engineContext.addProperties(advancedConfiguration);
            List systemProperties = (List) advancedConfiguration.get("systemProperties");
            if (systemProperties != null) {
                for (Object systemProperty : systemProperties) {
                    Map prop = (Map) systemProperty;
                    if (!(Boolean) prop.get("enable")) {
                        continue;
                    }
                    props.append(prop.get("name")).append("=").append(prop.get("value")).append(StringUtils.LF);
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
                byte[] fileContent = fileMetadataService.loadFileAsBytes(fileMetadata.getId());
                InputStream inputSource = new ByteArrayInputStream(fileContent);
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
    private void setFileMetadataService(FileMetadataService fileMetadataService) {
        EngineFactory.fileMetadataService = fileMetadataService;
    }

    @Resource
    public void setTestResourcePoolService(BaseTestResourcePoolService baseTestResourcePoolService) {
        EngineFactory.baseTestResourcePoolService = baseTestResourcePoolService;
    }

    @Resource
    public void setPerformanceReportService(PerformanceReportService performanceReportService) {
        EngineFactory.performanceReportService = performanceReportService;
    }
}
