package io.metersphere.performance.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.Application;
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
import io.metersphere.service.FileService;
import io.metersphere.service.KubernetesTestEngine;
import io.metersphere.service.TestResourcePoolService;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections8.Reflections;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EngineFactory {
    private static FileService fileService;
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
                return ConstructorUtils.invokeConstructor(kubernetesTestEngineClass, loadTest);
            } catch (Exception e) {
                LogUtil.error(e);
                return null;
            }
        }
        return null;
    }

    public static EngineContext createContext(LoadTestWithBLOBs loadTest, String resourceId, double ratio, long startTime, String reportId, int resourceIndex) {
        final List<FileMetadata> fileMetadataList = fileService.getFileMetadataByTestId(loadTest.getId());
        if (org.springframework.util.CollectionUtils.isEmpty(fileMetadataList)) {
            MSException.throwException(Translator.get("run_load_test_file_not_found") + loadTest.getId());
        }
        FileMetadata jmxFile = fileMetadataList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getType(), FileType.JMX.name()))
                .findFirst().orElseGet(() -> {
                    throw new RuntimeException(Translator.get("run_load_test_file_not_found") + loadTest.getId());
                });

        List<FileMetadata> csvFiles = fileMetadataList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getType(), FileType.CSV.name())).collect(Collectors.toList());
        List<FileMetadata> jarFiles = fileMetadataList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getType(), FileType.JAR.name())).collect(Collectors.toList());
        final FileContent fileContent = fileService.getFileContent(jmxFile.getId());
        if (fileContent == null) {
            MSException.throwException(Translator.get("run_load_test_file_content_not_found") + loadTest.getId());
        }
        final EngineContext engineContext = new EngineContext();
        engineContext.setTestId(loadTest.getId());
        engineContext.setTestName(loadTest.getName());
        engineContext.setNamespace(loadTest.getProjectId());
        engineContext.setFileType(jmxFile.getType());
        engineContext.setResourcePoolId(loadTest.getTestResourcePoolId());
        engineContext.setStartTime(startTime);
        engineContext.setReportId(reportId);
        engineContext.setResourceIndex(resourceIndex);

        if (StringUtils.isNotEmpty(loadTest.getLoadConfiguration())) {
            final JSONArray jsonArray = JSONObject.parseArray(loadTest.getLoadConfiguration());

            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i) instanceof Map) {
                    JSONObject o = jsonArray.getJSONObject(i);
                    String key = o.getString("key");
                    if ("TargetLevel".equals(key)) {
                        engineContext.addProperty(key, Math.round(((Integer) o.get("value")) * ratio));
                    } else {
                        engineContext.addProperty(key, o.get("value"));
                    }
                }
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
                                value = Math.round(((Integer) b.get("value")) * ratio);
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
        if (StringUtils.isNotEmpty(loadTest.getAdvancedConfiguration())) {
            JSONObject advancedConfiguration = JSONObject.parseObject(loadTest.getAdvancedConfiguration());
            engineContext.addProperties(advancedConfiguration);
        }

        final EngineSourceParser engineSourceParser = EngineSourceParserFactory.createEngineSourceParser(engineContext.getFileType());

        if (engineSourceParser == null) {
            MSException.throwException("File type unknown");
        }

        try (ByteArrayInputStream source = new ByteArrayInputStream(fileContent.getFile())) {
            String content = engineSourceParser.parse(engineContext, source);
            engineContext.setContent(content);
        } catch (MSException e) {
            LogUtil.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }

        if (CollectionUtils.isNotEmpty(csvFiles)) {
            Map<String, String> data = new HashMap<>();
            csvFiles.forEach(cf -> {
                FileContent csvContent = fileService.getFileContent(cf.getId());
                data.put(cf.getName(), new String(csvContent.getFile()));
            });
            engineContext.setTestData(data);
        }

        if (CollectionUtils.isNotEmpty(jarFiles)) {
            Map<String, byte[]> data = new HashMap<>();
            jarFiles.forEach(jf -> {
                FileContent content = fileService.getFileContent(jf.getId());
                data.put(jf.getName(), content.getFile());
            });
            engineContext.setTestJars(data);
        }

        return engineContext;
    }


    @Resource
    private void setFileService(FileService fileService) {
        EngineFactory.fileService = fileService;
    }

    @Resource
    public void setTestResourcePoolService(TestResourcePoolService testResourcePoolService) {
        EngineFactory.testResourcePoolService = testResourcePoolService;
    }
}
