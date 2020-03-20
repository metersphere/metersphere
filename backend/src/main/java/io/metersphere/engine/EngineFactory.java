package io.metersphere.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.commons.constants.EngineType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.engine.docker.DockerTestEngine;
import io.metersphere.engine.kubernetes.KubernetesTestEngine;
import io.metersphere.i18n.Translator;
import io.metersphere.parse.EngineSourceParser;
import io.metersphere.parse.EngineSourceParserFactory;
import io.metersphere.service.FileService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EngineFactory {
    private static FileService fileService;

    public static Engine createEngine(String engineType) {
        final EngineType type = EngineType.valueOf(engineType);

        switch (type) {
            case DOCKER:
                return new DockerTestEngine();
            case KUBERNETES:
                return new KubernetesTestEngine();
        }
        return null;
    }

    public static EngineContext createContext(LoadTestWithBLOBs loadTest, FileMetadata fileMetadata, List<FileMetadata> csvFiles) throws Exception {
        final FileContent fileContent = fileService.getFileContent(fileMetadata.getId());
        if (fileContent == null) {
            MSException.throwException(Translator.get("run_load_test_file_content_not_found") + loadTest.getId());
        }
        final EngineContext engineContext = new EngineContext();
        engineContext.setTestId(loadTest.getId());
        engineContext.setTestName(loadTest.getName());
        engineContext.setNamespace(loadTest.getProjectId());
        engineContext.setEngineType(fileMetadata.getEngine());
        engineContext.setFileType(fileMetadata.getType());

        if (!StringUtils.isEmpty(loadTest.getLoadConfiguration())) {
            final JSONArray jsonArray = JSONObject.parseArray(loadTest.getLoadConfiguration());

            for (int i = 0; i < jsonArray.size(); i++) {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);
                engineContext.addProperty(jsonObject.getString("key"), jsonObject.get("value"));
            }
        }

        final EngineSourceParser engineSourceParser = EngineSourceParserFactory.createEngineSourceParser(engineContext.getFileType());

        if (engineSourceParser == null) {
            MSException.throwException("File type unknown");
        }

        String content = engineSourceParser.parse(engineContext, new ByteArrayInputStream(fileContent.getFile()));
        engineContext.setContent(content);

        if (CollectionUtils.isNotEmpty(csvFiles)) {
            Map<String, String> data = new HashMap<>();
            csvFiles.forEach(cf -> {
                FileContent csvContent = fileService.getFileContent(cf.getId());
                data.put(cf.getName(), new String(csvContent.getFile()));
            });
            engineContext.setTestData(data);
        }

        return engineContext;
    }


    @Resource
    private void setFileService(FileService fileService) {
        EngineFactory.fileService = fileService;
    }
}
