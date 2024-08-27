package io.metersphere.api.curl.handler;

import io.metersphere.api.curl.constants.CurlPatternConstants;
import io.metersphere.api.curl.domain.CurlEntity;
import io.metersphere.api.utils.JSONUtil;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;


/**
 * @author wx
 */
public class HttpBodyHandler extends CurlHandlerChain {
    @Override
    public void handle(CurlEntity entity, String curl) {
        JSONObject body = parseBody(curl);
        entity.setBody(body);
        super.nextHandle(entity, curl);
    }

    /**
     * 请求体解析
     *
     * @param curl
     * @return
     */
    private JSONObject parseBody(String curl) {
        Matcher formMatcher = CurlPatternConstants.HTTP_FROM_BODY_PATTERN.matcher(curl);
        if (formMatcher.find()) {
            return parseFormBody(formMatcher);
        }

        Matcher urlencodeMatcher = CurlPatternConstants.HTTP_URLENCODE_BODY_PATTERN.matcher(curl);
        if (urlencodeMatcher.find()) {
            return parseUrlEncodeBody(urlencodeMatcher);
        }

        Matcher rawMatcher = CurlPatternConstants.HTTP_ROW_BODY_PATTERN.matcher(curl);
        if (rawMatcher.find()) {
            return parseRowBody(rawMatcher);
        }

        Matcher defaultMatcher = CurlPatternConstants.DEFAULT_HTTP_BODY_PATTERN.matcher(curl);
        if (defaultMatcher.find()) {
            return parseDefaultBody(defaultMatcher);
        }

        return new JSONObject();
    }

    private JSONObject parseDefaultBody(Matcher defaultMatcher) {
        String bodyStr = "";
        if (defaultMatcher.group(1) != null) {
            //单引号数据
            bodyStr = defaultMatcher.group(1);
        } else if (defaultMatcher.group(2) != null) {
            //双引号数据
            bodyStr = defaultMatcher.group(2);
        } else {
            //无引号数据
            bodyStr = defaultMatcher.group(3);
        }

        if (isJSON(bodyStr)) {
            return JSONUtil.parseObject(bodyStr);
        }

        //其他格式 a=b&c=d
        Matcher kvMatcher = CurlPatternConstants.DEFAULT_HTTP_BODY_PATTERN_KV.matcher(bodyStr);
        return kvMatcher.matches() ? parseKVBody(bodyStr) : new JSONObject();
    }

    private JSONObject parseKVBody(String kvBodyStr) {
        JSONObject json = new JSONObject();
        String[] pairs = kvBodyStr.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            json.put(key, value);
        }
        return json;
    }

    private JSONObject parseFormBody(Matcher formMatcher) {
        JSONObject formData = new JSONObject();

        formMatcher.reset();
        while (formMatcher.find()) {
            //提取表单
            String formItem = formMatcher.group(1) != null ? formMatcher.group(1) : formMatcher.group(2);
            String[] keyValue = formItem.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];

                //文件属性
                if (value.startsWith("@")) {
                    //获取文件名
                    formData.put(key, value.substring(1));
                } else {
                    formData.put(key, value);
                }
            }
        }

        return formData;
    }

    private JSONObject parseUrlEncodeBody(Matcher urlencodeMatcher) {
        JSONObject urlEncodeData = new JSONObject();
        urlencodeMatcher.reset();
        while (urlencodeMatcher.find()) {
            String keyValueEncoded = urlencodeMatcher.group(1);
            String[] keyValue = keyValueEncoded.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
                urlEncodeData.put(key, decodedValue);
            }
        }
        return urlEncodeData;
    }

    private JSONObject parseRowBody(Matcher rowMatcher) {
        String rawData = rowMatcher.group(1);

        if (isXML(rawData)) {
            return xml2json(rawData);
        }

        try {
            return JSONUtil.parseObject(rawData);
        } catch (Exception e) {
            throw new MSException(Translator.get("curl_raw_content_is_invalid"), e);
        }
    }

    private boolean isJSON(String jsonStr) {
        try {
            JSONUtil.parseObject(jsonStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isXML(String xmlStr) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("disallow-doctype-decl", false);
            factory.setFeature("external-general-entities", false);
            factory.setFeature("external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlStr));
            builder.parse(is);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private JSONObject xml2json(String xmlStr) {
        try {
            JSONObject orgJsonObj = XML.toJSONObject(xmlStr);
            String jsonString = orgJsonObj.toString();
            return JSONUtil.parseObject(jsonString);
        } catch (JSONException e) {
            throw new MSException(Translator.get("curl_raw_content_is_invalid"), e);
        }
    }

}