package io.metersphere.api.dto.scenario;

import io.metersphere.api.exec.generator.JSONSchemaBuilder;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.request.BodyFile;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Body {
    private String type;
    private String raw;
    private String format;
    private List<KeyValue> kvs;
    private List<KeyValue> binary;
    private Object jsonSchema;
    private String tmpFilePath;
    public final static String KV = "KeyValue";
    public final static String FORM_DATA = "Form Data";
    public final static String WWW_FROM = "WWW_FORM";
    public final static String RAW = "Raw";
    public final static String BINARY = "BINARY";
    public final static String JSON_STR = "JSON";
    public final static String XML = "XML";
    public final static String JSON_SCHEMA = "JSON-SCHEMA";

    public boolean isValid() {
        if (this.isKV()) {
            if (kvs != null) {
                return kvs.stream().anyMatch(KeyValue::isValid);
            }
        } else {
            return StringUtils.isNotBlank(raw);
        }
        return false;
    }

    public boolean isKV() {
        if (StringUtils.equals(type, FORM_DATA) || StringUtils.equals(type, WWW_FROM)
                || StringUtils.equals(type, BINARY)) {
            return true;
        } else {
            return false;
        }
    }

    public List<KeyValue> getBodyParams(HTTPSamplerProxy sampler, String requestId) {
        List<KeyValue> body = new ArrayList<>();
        if (this.isKV() || this.isBinary()) {
            if (StringUtils.equalsAnyIgnoreCase(this.type, WWW_FROM, FORM_DATA)) {
                body = this.getKvs().stream().filter(KeyValue::isValid).collect(Collectors.toList());
            }
            // 处理上传文件
            HTTPFileArg[] httpFileArgs = httpFileArgs(requestId);
            if (ArrayUtils.isNotEmpty(httpFileArgs)) {
                sampler.setHTTPFiles(httpFileArgs(requestId));
                sampler.setDoMultipart(!StringUtils.equalsIgnoreCase(this.type, "BINARY"));
            }
        } else {
            if (StringUtils.isNotEmpty(this.getRaw()) || this.getJsonSchema() != null) {
                analyticalData();
                KeyValue keyValue = new KeyValue(StringUtils.EMPTY, JSON_SCHEMA, this.getRaw(), true, true);
                sampler.setPostBodyRaw(true);
                keyValue.setEnable(true);
                keyValue.setUrlEncode(false);
                body.add(keyValue);
            }
        }
        return body;
    }

    private void parseMock() {
        if (StringUtils.isNotEmpty(this.getRaw())) {
            String value = StringUtils.chomp(this.getRaw().trim());
            if (StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) {
                List list = JSON.parseArray(this.getRaw());
                if (!this.getRaw().contains(ElementConstants.REF)) {
                    jsonMockParse(list);
                }
                this.raw = JSONUtil.toJSONString(list);
            } else {
                Map<String, Object> map = JSON.parseObject(this.getRaw(), Map.class);
                if (!this.getRaw().contains(ElementConstants.REF)) {
                    jsonMockParse(map);
                }
                this.raw = JSONUtil.toJSONString(map);
            }
        }
    }

    private void analyticalData() {
        try {
            boolean isArray = false;
            if (StringUtils.isNotBlank(this.type) && StringUtils.equals(this.type, JSON_STR)) {
                if (StringUtils.isNotEmpty(this.format) && this.getJsonSchema() != null && JSON_SCHEMA.equals(this.format)) {
                    this.raw = StringEscapeUtils.unescapeJava(JSONSchemaBuilder.generator(JSONUtil.toJSONString(this.getJsonSchema())));
                } else {
                    parseMock();
                }
                // 格式化处理
                if (isArray) {
                    this.raw = JSONUtil.parserArray(this.raw);
                } else {
                    this.raw = JSONUtil.parserObject(this.raw);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("json mock value is abnormal", e);
        }
    }

    private void jsonMockParse(Map map) {
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof List) {
                jsonMockParse((List) value);
            } else if (value instanceof Map) {
                jsonMockParse((Map) value);
            } else if (value instanceof String) {
                if (StringUtils.isNotBlank((String) value)) {
                    value = ScriptEngineUtils.buildFunctionCallString((String) value);
                }
                map.put(key, value);
            }
        }
    }

    private void jsonMockParse(List list) {

        Map<Integer, String> replaceDataMap = new HashMap<>();
        for (int index = 0; index < list.size(); index++) {
            Object obj = list.get(index);
            if (obj instanceof Map) {
                jsonMockParse((Map) obj);
            } else if (obj instanceof String) {
                if (StringUtils.isNotBlank((String) obj)) {
                    String str = ScriptEngineUtils.buildFunctionCallString((String) obj);
                    replaceDataMap.put(index, str);
                }
            }
        }

        for (Map.Entry<Integer, String> entry : replaceDataMap.entrySet()) {
            int replaceIndex = entry.getKey();
            String replaceStr = entry.getValue();
            list.set(replaceIndex, replaceStr);
        }
    }

    private HTTPFileArg[] httpFileArgs(String requestId) {
        List<HTTPFileArg> list = new ArrayList<>();
        if (StringUtils.equalsAnyIgnoreCase(this.type, WWW_FROM, FORM_DATA) && CollectionUtils.isNotEmpty(this.getKvs())) {
            this.getKvs().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
                setFileArg(list, keyValue.getFiles(), keyValue, requestId, false);
            });
        }
        if (StringUtils.equalsIgnoreCase(this.type, BINARY) && CollectionUtils.isNotEmpty(this.getBinary())) {
            this.getBinary().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
                setFileArg(list, keyValue.getFiles(), keyValue, requestId, true);
            });
        }
        return list.toArray(new HTTPFileArg[0]);
    }

    private void setFileArg(List<HTTPFileArg> list, List<BodyFile> files,
                            KeyValue keyValue, String requestId, boolean isBinary) {
        if (files != null) {
            files.forEach(file -> {
                boolean isRef = false;
                String fileId = null;
                String paramName = keyValue.getName() == null ? requestId : keyValue.getName();
                String path = null;
                if (StringUtils.equalsIgnoreCase(file.getStorage(), StorageConstants.FILE_REF.name())) {
                    isRef = true;
                    fileId = file.getFileId();
                    path = FileUtils.getFilePath(file);
                } else if (StringUtils.isNotBlank(file.getId()) && !isBinary) {
                    // 旧数据
                    path = FileUtils.BODY_FILE_DIR + '/' + file.getId() + '_' + file.getName();
                } else if (StringUtils.isNotBlank(this.tmpFilePath)) {
                    path = FileUtils.BODY_FILE_DIR + '/' + this.tmpFilePath + '/' + file.getName();
                } else {
                    path = FileUtils.BODY_FILE_DIR + '/' + requestId + '/' + file.getName();
                }
                String mimetype = keyValue.getContentType();
                if (StringUtils.isBlank(mimetype)) {
                    mimetype = ContentType.APPLICATION_OCTET_STREAM.getMimeType();
                }
                HTTPFileArg fileArg = new HTTPFileArg(path, isBinary ? StringUtils.EMPTY : paramName, mimetype);
                fileArg.setProperty(ElementConstants.IS_REF, isRef);
                fileArg.setProperty(ElementConstants.FILE_ID, fileId);
                fileArg.setProperty(ElementConstants.RESOURCE_ID, requestId);
                list.add(fileArg);
            });
        }
    }

    public boolean isBinary() {
        return StringUtils.equals(type, BINARY);
    }

    public boolean isJson() {
        return StringUtils.equals(type, JSON_STR);
    }

    public boolean isXml() {
        return StringUtils.equals(type, XML);
    }

    public void init() {
        this.type = StringUtils.EMPTY;
        this.raw = StringUtils.EMPTY;
        this.format = StringUtils.EMPTY;
    }

    public void initKvs() {
        this.kvs = new ArrayList<>();
        this.kvs.add(new KeyValue());
    }

    public void initBinary() {
        this.binary = new ArrayList<>();
        this.binary.add(new KeyValue());
    }
}
