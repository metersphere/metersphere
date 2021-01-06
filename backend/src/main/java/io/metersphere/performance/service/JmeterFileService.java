package io.metersphere.performance.service;


import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.engine.EngineContext;
import io.metersphere.performance.engine.EngineFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class JmeterFileService {

    @Resource
    private LoadTestMapper loadTestMapper;

    public byte[] downloadZip(String testId, String resourceId, double ratio, long startTime, String reportId, int resourceIndex, int threadNum) {
        try {
            LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(testId);
            // deep copy
            LoadTestWithBLOBs subTest = SerializationUtils.clone(loadTest);
            setThreadNum(subTest, threadNum);
            EngineContext context = EngineFactory.createContext(subTest, resourceId, ratio, startTime, reportId, resourceIndex);
            return zipFilesToByteArray(context);
        } catch (MSException e) {
            LogUtil.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }
        return null;
    }

    private void setThreadNum(LoadTestWithBLOBs t, Integer limit) {
        String loadConfiguration = t.getLoadConfiguration();
        JSONArray jsonArray = JSON.parseArray(loadConfiguration);
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i) instanceof Map) {
                JSONObject o = jsonArray.getJSONObject(i);
                if (StringUtils.equals(o.getString("key"), "TargetLevel")) {
                    o.put("value", limit);
                    break;
                }
            }
            if (jsonArray.get(i) instanceof List) {
                JSONArray o = jsonArray.getJSONArray(i);
                for (int j = 0; j < o.size(); j++) {
                    JSONObject b = o.getJSONObject(j);
                    if (StringUtils.equals(b.getString("key"), "TargetLevel")) {
                        b.put("value", limit);
                        break;
                    }
                }
            }
        }
        // 设置线程数
        t.setLoadConfiguration(jsonArray.toJSONString());
    }

    private byte[] zipFilesToByteArray(EngineContext context) throws IOException {
        String testId = context.getTestId();
        String fileName = testId + ".jmx";

        Map<String, byte[]> files = new HashMap<>();

        //  每个测试生成一个文件夹
        files.put(fileName, context.getContent().getBytes(StandardCharsets.UTF_8));
        // 保存测试数据文件
        Map<String, String> testData = context.getTestData();
        if (!CollectionUtils.isEmpty(testData)) {
            for (String k : testData.keySet()) {
                String v = testData.get(k);
                files.put(k, v.getBytes(StandardCharsets.UTF_8));
            }
        }

        // 保存 byte[] jar
        Map<String, byte[]> jarFiles = context.getTestJars();
        if (!CollectionUtils.isEmpty(jarFiles)) {
            for (String k : jarFiles.keySet()) {
                byte[] v = jarFiles.get(k);
                files.put(k, v);
            }
        }

        return listBytesToZip(files);
    }


    private byte[] listBytesToZip(Map<String, byte[]> mapReport) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (Map.Entry<String, byte[]> report : mapReport.entrySet()) {
            ZipEntry entry = new ZipEntry(report.getKey());
            entry.setSize(report.getValue().length);
            zos.putNextEntry(entry);
            zos.write(report.getValue());
        }
        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }
}
