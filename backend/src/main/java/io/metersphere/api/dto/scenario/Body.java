package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.commons.json.JSONSchemaGenerator;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    public final static String KV = "KeyValue";
    public final static String FORM_DATA = "Form Data";
    public final static String WWW_FROM = "WWW_FORM";
    public final static String RAW = "Raw";
    public final static String BINARY = "BINARY";
    public final static String JSON = "JSON";
    public final static String XML = "XML";

    public boolean isValid() {
        if (this.isKV()) {
            return kvs.stream().anyMatch(KeyValue::isValid);
        } else {
            return StringUtils.isNotBlank(raw);
        }
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
            body = this.getKvs().stream().filter(KeyValue::isValid).collect(Collectors.toList());
            HTTPFileArg[] httpFileArgs = httpFileArgs(requestId);
            // 文件上传
            if (httpFileArgs.length > 0) {
                sampler.setHTTPFiles(httpFileArgs(requestId));
                sampler.setDoMultipart(true);
            }
        } else {
            if (!this.isJson()) {
                sampler.setPostBodyRaw(true);
            } else {
                if (StringUtils.isNotEmpty(this.format) && "JSON-SCHEMA".equals(this.format) && this.getJsonSchema() != null) {
                    this.raw = JSONSchemaGenerator.getJson(com.alibaba.fastjson.JSON.toJSONString(this.getJsonSchema()));
                }
            }
            KeyValue keyValue = new KeyValue("", "JSON-SCHEMA", this.getRaw(), true, true);
            keyValue.setEnable(true);
            keyValue.setEncode(false);
            body.add(keyValue);
        }
        return body;
    }

    private HTTPFileArg[] httpFileArgs(String requestId) {
        List<HTTPFileArg> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(this.getKvs())) {
            this.getKvs().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
                setFileArg(list, keyValue.getFiles(), keyValue, requestId);
            });
        }
        if (CollectionUtils.isNotEmpty(this.getBinary())) {
            this.getBinary().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
                setFileArg(list, keyValue.getFiles(), keyValue, requestId);
            });
        }
        return list.toArray(new HTTPFileArg[0]);
    }

    private void setFileArg(List<HTTPFileArg> list, List<BodyFile> files, KeyValue keyValue, String requestId) {
        final String BODY_FILE_DIR = "/opt/metersphere/data/body";
        if (files != null) {
            files.forEach(file -> {
                String paramName = keyValue.getName() == null ? requestId : keyValue.getName();
                String path = BODY_FILE_DIR + '/' + file.getId() + '_' + file.getName();
                String mimetype = keyValue.getContentType();
                list.add(new HTTPFileArg(path, paramName, mimetype));
            });
        }
    }

    public boolean isBinary() {
        return StringUtils.equals(type, BINARY);
    }

    public boolean isJson() {
        return StringUtils.equals(type, JSON);
    }

    public boolean isXml() {
        return StringUtils.equals(type, XML);
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
