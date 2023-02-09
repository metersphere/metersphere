package io.metersphere.api.jmeter.utils;

import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.dto.AttachmentBodyFile;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JmxFileUtil {
    public static final String REDIS_JMX_EXECUTE_FILE_PREFIX = "JMX.FILE.";

    private static final String JMX_INFO_KEY_ID = "id";
    private static final String JMX_INFO_KEY_FILE_PATH = "filePath";

    private static final String JMX_INFO_KEY_FILE_NAME = "fileName";

    private static final String JMX_INFO_KEY_FILE_STORAGE = "storage";

    public static String getExecuteFileKeyInRedis(String reportId) {
        return StringUtils.join(REDIS_JMX_EXECUTE_FILE_PREFIX, reportId);
    }

    public static String getExecuteScriptKey(String reportId, String testId) {
        return StringUtils.join(reportId, "-", testId);
    }

    public static String getRedisJmxFileString(List<AttachmentBodyFile> listFile) {
        List<Map<String, String>> jmxFileList = new ArrayList<>();
        listFile.forEach(file -> {
            Map<String, String> fileInfoMap = new HashMap<>();
            if (StringUtils.isNotBlank(file.getFileMetadataId())) {
                fileInfoMap.put(JMX_INFO_KEY_ID, file.getFileMetadataId());
            }
            if (StringUtils.isNotBlank(file.getFilePath())) {
                fileInfoMap.put(JMX_INFO_KEY_FILE_PATH, file.getFilePath());
            }
            if (StringUtils.isNotBlank(file.getName())) {
                fileInfoMap.put(JMX_INFO_KEY_FILE_NAME, file.getName());
            }
            if (StringUtils.isNotBlank(file.getFileStorage())) {
                fileInfoMap.put(JMX_INFO_KEY_FILE_STORAGE, file.getFileStorage());
            }

            if (MapUtils.isNotEmpty(fileInfoMap)) {
                jmxFileList.add(fileInfoMap);
            }
        });
        return JSONUtil.toJSONString(jmxFileList);
    }

    public static List<AttachmentBodyFile> formatRedisJmxFileString(Object attachmentFileObj) {
        List<AttachmentBodyFile> returnList = new ArrayList<>();
        try {
            if (attachmentFileObj != null) {
                List<Map> list = JSONUtil.parseArray(attachmentFileObj.toString(), Map.class);
                list.forEach(itemMap -> {
                    AttachmentBodyFile file = new AttachmentBodyFile();
                    if (itemMap.containsKey(JMX_INFO_KEY_ID)) {
                        file.setFileMetadataId(itemMap.get(JMX_INFO_KEY_ID).toString());
                    }
                    if (itemMap.containsKey(JMX_INFO_KEY_FILE_PATH)) {
                        file.setFilePath(itemMap.get(JMX_INFO_KEY_FILE_PATH).toString());
                    }
                    if (itemMap.containsKey(JMX_INFO_KEY_FILE_NAME)) {
                        file.setName(itemMap.get(JMX_INFO_KEY_FILE_NAME).toString());
                    }
                    if (itemMap.containsKey(JMX_INFO_KEY_FILE_STORAGE)) {
                        file.setFileStorage(itemMap.get(JMX_INFO_KEY_FILE_STORAGE).toString());
                    }
                    returnList.add(file);
                });
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
        }
        return returnList;
    }
}
