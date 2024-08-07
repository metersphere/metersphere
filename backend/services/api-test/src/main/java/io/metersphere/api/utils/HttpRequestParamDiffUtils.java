package io.metersphere.api.utils;

import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.JsonBody;
import io.metersphere.api.dto.request.http.body.XmlBody;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.XMLUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-01  14:01
 */
public class HttpRequestParamDiffUtils {

    public static boolean isRequestParamDiff(Object request1, Object request2) {
        if (!(request1 instanceof MsHTTPElement)) {
            // 其他协议不比较
            return false;
        }
        MsHTTPElement httpElement1 = (MsHTTPElement) request1;
        MsHTTPElement httpElement2 = (MsHTTPElement) request2;
        boolean isQueryDiff = isParamKeyDiff(httpElement1.getQuery(), httpElement2.getQuery());
        boolean isRestDiff = isParamKeyDiff(httpElement1.getRest(), httpElement2.getRest());
        boolean isHeaderDiff = isParamKeyDiff(httpElement1.getHeaders(), httpElement2.getHeaders());
        boolean isBodyDiff = isBodyDiff(httpElement1.getBody(), httpElement2.getBody());
        if (isQueryDiff || isRestDiff || isHeaderDiff || isBodyDiff) {
            return true;
        }
        return false;
    }

    public static boolean isBodyDiff(Body body1, Body body2) {
        if (body1 == null && body2 == null) {
            return false;
        }
        if (body1 == null || body2 == null) {
            return true;
        }
        if (body1.getBodyType() != body2.getBodyType()) {
            // 类型不一样，则发生变更
            return true;
        }
        Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, body1.getBodyType());
        switch (bodyType) {
            case FORM_DATA:
                return isParamKeyDiff(body1.getFormDataBody().getFormValues(), body2.getFormDataBody().getFormValues());
            case WWW_FORM:
                return isParamKeyDiff(body1.getWwwFormBody().getFormValues(), body2.getWwwFormBody().getFormValues());
            case JSON:
                return isJsonBodyDiff(body1.getJsonBody(), body2.getJsonBody());
            case XML:
                return isXmlBodyDiff(body1.getXmlBody(), body2.getXmlBody());
            default:
                // RAW,BINARY 不比较
                return false;
        }
    }

    public static boolean isJsonBodyDiff(JsonBody jsonBody1, JsonBody jsonBody2) {
        String jsonValue1 = jsonBody1.getJsonValue();
        String jsonValue2 = jsonBody2.getJsonValue();
        if (StringUtils.isBlank(jsonValue1) && StringUtils.isBlank(jsonValue2)) {
            return false;
        }
        if (StringUtils.isBlank(jsonValue1) || StringUtils.isBlank(jsonValue2)) {
            return true;
        }

        try {
            Object json1 = JSON.parseObject(jsonValue1);
            Object json2 = JSON.parseObject(jsonValue2);
            return !getBlankJon(json1).equals(getBlankJon(json2));
        } catch (Exception e) {
            LogUtils.info("json 解析异常，json1: {}, json2: {}", jsonValue1, jsonValue2);
            return !getJsonKeys(jsonValue1).equals(getJsonKeys(jsonValue2));
        }
    }

    /**
     * 将json对象的属性值都置空
     * 便于比较参数名是否一致
     * @param obj
     * @return
     */
    public static Object getBlankJon(Object obj) {
        if (obj == null) {
            return StringUtils.EMPTY;
        }
        if (obj instanceof Map map) {
            map.keySet().forEach(key -> {
                Object value = map.get(key);
                map.put(key, getBlankJon(value));
            });
            return map;
        } else if (obj instanceof List list) {
            if (CollectionUtils.isEmpty(list)) {
                return list;
            }
            Object first = list.getFirst();
            // 数组只获取第一个元素进行比较
            return new ArrayList<>(List.of(getBlankJon(first)));
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 从 json 串中获取 key 列表
     * 因为数值类型使用 mock 函数，会导致 json 串为非法 json 串
     * 这里使用正则表达式获取key
     * 使用 LinkedHashSet 按序获取，近似比较两个 json 串的 key
     * @param jsonValue
     * @return
     */
    public static Set<String> getJsonKeys(String jsonValue) {
        String pattern = "\"([^\"]*)\"\\s*:";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(jsonValue);
        Set<String> keys = new LinkedHashSet<>();
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }

    public static boolean isXmlBodyDiff(XmlBody xmlBody1, XmlBody xmlBody2) {
        String value1 = xmlBody1.getValue();
        String value2 = xmlBody2.getValue();
        if (StringUtils.isBlank(value1) && StringUtils.isBlank(value2)) {
            return false;
        }
        if (StringUtils.isBlank(value1) || StringUtils.isBlank(value2)) {
            return true;
        }
        try {
            Set<String> keySet1 = XMLUtils.elementToMap(XMLUtils.stringToDocument(value1).getRootElement()).keySet();
            Set<String> keySet2 = XMLUtils.elementToMap(XMLUtils.stringToDocument(value2).getRootElement()).keySet();
            return !keySet1.equals(keySet2);
        } catch (Exception e) {
            return !StringUtils.equals(value1, value2);
        }
    }

    public static boolean isParamKeyDiff(List<? extends KeyValueParam> params1, List<? extends KeyValueParam> params2) {
        params1 = params1 == null ? List.of() : params1;
        params2 = params2 == null ? List.of() : params2;
        Set<String> keSet1 = params1.stream()
                .filter(KeyValueParam::isValid)
                .map(KeyValueParam::getKey)
                .collect(Collectors.toSet());
        Set<String> keSet2 = params2.stream()
                .filter(KeyValueParam::isValid)
                .map(KeyValueParam::getKey)
                .collect(Collectors.toSet());
        return !keSet1.equals(keSet2);
    }

    /**
     * 将 json 和 xml 属性值置空
     * 便于前端比较差异
     * @param httpElement
     * @return
     */
    public static AbstractMsTestElement getCompareHttpElement(MsHTTPElement httpElement) {
        Body body = httpElement.getBody();
        if (body == null) {
            return httpElement;
        }
        if (StringUtils.equals(body.getBodyType(), Body.BodyType.JSON.name())) {
            try {
                String jsonValue = body.getJsonBody().getJsonValue();
                jsonValue = replaceIllegalJsonWithMock(jsonValue);
                Object blankJon = getBlankJon(JSON.parseObject(jsonValue));
                body.getJsonBody().setJsonValue(JSON.toFormatJSONString(blankJon));
            } catch (Exception e) {
                LogUtils.info("json 解析异常，json: {}", body.getJsonBody().getJsonValue());
            }
        }
        if (StringUtils.equals(body.getBodyType(), Body.BodyType.XML.name())) {
            String xml = body.getXmlBody().getValue();
            try {
                Element element = XMLUtils.stringToDocument(xml).getRootElement();
                XMLUtils.clearElementText(element);
                StringWriter stringWriter = new StringWriter();
                XMLWriter writer = new XMLWriter(stringWriter);
                writer.write(element);
                xml = stringWriter.toString();
            } catch (Exception e) {
                LogUtils.info("xml 解析异常，xml: {}", body.getXmlBody().getValue());
                xml = XMLUtils.clearElementText(xml);
            }
            body.getXmlBody().setValue(xml);
        }
        return httpElement;
    }

    /**
     * 如果 json 串中包含了非字符串的 mock 函数
     * 例如：
     * {"a": @integer(1, 2)}
     * 替换成空字符串
     * {"a": ""}
     * 避免 json 序列化失败
     * @param text
     * @return
     */
    public static String replaceIllegalJsonWithMock(String text) {
        String pattern = ":\\s*(@\\w+(\\(\\s*\\w*\\s*,?\\s*\\w*\\s*\\))*)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        while (matcher.find()) {
            // 这里连同:一起替换，避免替换了其他合规的参数值
            text = text.replaceFirst(pattern, ":\"\"");
        }
        return text;
    }
}
