package io.metersphere.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.commons.constants.EngineType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.engine.jmx.JmxEngine;
import io.metersphere.parse.EngineSourceParser;
import io.metersphere.parse.EngineSourceParserFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class EngineFactory {
    public static Engine createEngine(String engineType) {
        final EngineType type = EngineType.valueOf(engineType);

        if (type == EngineType.JMX) {
            return new JmxEngine();
        }
        return null;
    }

    public static EngineContext createContext(LoadTestWithBLOBs loadTest, FileMetadata fileMetadata, FileContent fileContent) throws Exception {
        final EngineContext engineContext = new EngineContext();
        engineContext.setEngineId(loadTest.getId());
        engineContext.setInputStream(new ByteArrayInputStream(fileContent.getFile()));
        engineContext.setEngineType(fileMetadata.getType());

        if (!StringUtils.isEmpty(loadTest.getLoadConfiguration())) {
            final JSONArray jsonArray = JSONObject.parseArray(loadTest.getLoadConfiguration());

            for (int i = 0; i < jsonArray.size(); i++) {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);
                engineContext.addProperty(jsonObject.getString("key"), jsonObject.get("value"));
            }
        }

        final EngineSourceParser engineSourceParser = EngineSourceParserFactory.createEngineSourceParser(engineContext.getEngineType());

        if (engineSourceParser == null) {
            MSException.throwException("未知的文件类型！");
        }

        final InputStream inputStream = engineSourceParser.parse(engineContext, engineContext.getInputStream());
        engineContext.setInputStream(inputStream);

        return engineContext;
    }
}
