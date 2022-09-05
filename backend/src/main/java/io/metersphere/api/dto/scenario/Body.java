package io.metersphere.api.dto.scenario;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.json.JSONSchemaRunTest;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
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
import java.util.List;
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

    public boolean isOldKV() {
        if (StringUtils.equals(type, KV)) {
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
                parseJonBodyMock();
                KeyValue keyValue = new KeyValue("", "JSON-SCHEMA", this.getRaw(), true, true);
                sampler.setPostBodyRaw(true);
                keyValue.setEnable(true);
                keyValue.setUrlEncode(false);
                body.add(keyValue);
            }
        }
        return body;
    }

    private void parseJonBodyMock() {
        if (StringUtils.isNotBlank(this.type) && StringUtils.equals(this.type, "JSON")) {
            if (StringUtils.isNotEmpty(this.format) && this.getJsonSchema() != null
                    && "JSON-SCHEMA".equals(this.format)) {
                this.raw = StringEscapeUtils.unescapeJava(JSONSchemaRunTest.getJson(JSON.toJSONString(this.getJsonSchema())));
            } else {
                try {
                    if (StringUtils.isNotEmpty(this.getRaw())) {
                        JSONObject jsonObject = JSON.parseObject(this.getRaw(), Feature.OrderedField, Feature.DisableSpecialKeyDetect);
                        if (!this.getRaw().contains("$ref")) {
                            jsonMockParse(jsonObject);
                        }
                        this.raw = JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
                    }
                } catch (Exception e) {
                    LoggerUtil.error("json mock value is abnormal", e);
                }
            }
        }
    }

    private void jsonMockParse(JSONObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                jsonMockParse((JSONObject) value);
            } else if (value instanceof String) {
                if (StringUtils.isNotBlank((String) value)) {
                    value = ScriptEngineUtils.buildFunctionCallString((String) value);
                }
                jsonObject.put(key, value);
            }
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
                HTTPFileArg fileArg = new HTTPFileArg(path, isBinary ? "" : paramName, mimetype);
                fileArg.setProperty("isRef", isRef);
                fileArg.setProperty("fileId", fileId);
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
        this.type = "";
        this.raw = "";
        this.format = "";
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
