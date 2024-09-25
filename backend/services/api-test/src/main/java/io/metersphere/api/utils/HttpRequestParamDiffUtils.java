package io.metersphere.api.utils;

import io.metersphere.api.dto.definition.ApiCaseSyncItemRequest;
import io.metersphere.api.dto.definition.ApiCaseSyncRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.XMLUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.metersphere.sdk.util.XMLUtils.elementToMap;

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
        if (!StringUtils.equals(body1.getBodyType(), body2.getBodyType())) {
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
     *
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
     *
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
            Set<String> keySet1 = elementToMap(XMLUtils.stringToDocument(value1).getRootElement()).keySet();
            Set<String> keySet2 = elementToMap(XMLUtils.stringToDocument(value2).getRootElement()).keySet();
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
     *
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
            String xml = Optional.ofNullable(body.getXmlBody().getValue()).orElse(StringUtils.EMPTY);
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
     *
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

    /**
     * 同步请求参数
     * @param request
     * @param sourceElement
     * @param targetElement
     * @return
     */
    public static AbstractMsTestElement syncRequestDiff(ApiCaseSyncRequest request,
                                                        AbstractMsTestElement sourceElement, AbstractMsTestElement targetElement) {
        if (!(sourceElement instanceof MsHTTPElement)) {
            // 其他协议不比较
            return targetElement;
        }
        MsHTTPElement sourceHttpElement = (MsHTTPElement) sourceElement;
        MsHTTPElement targetHttpElement = (MsHTTPElement) targetElement;
        boolean isDeleteRedundantParam = BooleanUtils.isTrue(request.getDeleteRedundantParam());
        ApiCaseSyncItemRequest syncItems = request.getSyncItems();
        if (BooleanUtils.isTrue(syncItems.getHeader())) {
            targetHttpElement.setHeaders(
                    syncKeyValueParamDiff(isDeleteRedundantParam, sourceHttpElement.getHeaders(), targetHttpElement.getHeaders())
            );
        }

        if (BooleanUtils.isTrue(syncItems.getRest())) {
            targetHttpElement.setRest(
                    syncKeyValueParamDiff(isDeleteRedundantParam, sourceHttpElement.getRest(), targetHttpElement.getRest())
            );
        }

        if (BooleanUtils.isTrue(syncItems.getQuery())) {
            targetHttpElement.setQuery(
                    syncKeyValueParamDiff(isDeleteRedundantParam, sourceHttpElement.getQuery(), targetHttpElement.getQuery())
            );
        }

        if (BooleanUtils.isTrue(syncItems.getBody())) {
            Body sourceBody = sourceHttpElement.getBody();
            Body targetBody = targetHttpElement.getBody();
            targetBody = syncBodyDiff(isDeleteRedundantParam, sourceBody, targetBody);
            targetHttpElement.setBody(targetBody);
        }

        return targetElement;
    }

    /**
     * 同步 body 参数
     * @param isDeleteRedundantParam
     * @param sourceBody
     * @param targetBody
     * @return
     */
    public static Body syncBodyDiff(boolean isDeleteRedundantParam, Body sourceBody, Body targetBody) {
        if (sourceBody == null || targetBody == null || !StringUtils.equals(sourceBody.getBodyType(), targetBody.getBodyType())) {
            return sourceBody;
        }
        Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, sourceBody.getBodyType());
        switch (bodyType) {
            case FORM_DATA -> {
                List<FormDataKV> formDataKVS = syncKeyValueParamDiff(isDeleteRedundantParam, sourceBody.getFormDataBody().getFormValues(), targetBody.getFormDataBody().getFormValues());
                targetBody.getFormDataBody().setFormValues(formDataKVS);
            }
            case WWW_FORM -> {
                List<WWWFormKV> wwwFormKVS = syncKeyValueParamDiff(isDeleteRedundantParam, sourceBody.getWwwFormBody().getFormValues(), targetBody.getWwwFormBody().getFormValues());
                targetBody.getWwwFormBody().setFormValues(wwwFormKVS);
            }
            case JSON -> {
                JsonBody jsonBody = syncJsonBodyDiff(isDeleteRedundantParam, sourceBody.getJsonBody(), targetBody.getJsonBody());
                targetBody.setJsonBody(jsonBody);
            }
            case XML -> {
                XmlBody xmlBody = syncXmlBodyDiff(isDeleteRedundantParam, sourceBody.getXmlBody(), targetBody.getXmlBody());
                targetBody.setXmlBody(xmlBody);
            }
            default -> {}
            // RAW,BINARY 不同步
        }
        return targetBody;
    }

    /**
     * 同步 json 参数
     * @param isDeleteRedundantParam
     * @param sourceBody
     * @param targetBody
     * @return
     */
    public static JsonBody syncJsonBodyDiff(boolean isDeleteRedundantParam, JsonBody sourceBody, JsonBody targetBody) {
        if (sourceBody == null) {
            return isDeleteRedundantParam ? new JsonBody() : targetBody;
        }
        if (targetBody == null) {
            return sourceBody;
        }

        String sourceJsonStr = sourceBody.getJsonValue();
        String targetJsonStr = targetBody.getJsonValue();
        if (StringUtils.isBlank(sourceJsonStr)) {
            return isDeleteRedundantParam ? new JsonBody() : targetBody;
        }
        if (StringUtils.isBlank(targetJsonStr)) {
            return sourceBody;
        }

        try {
            Object sourceJson = JSON.parseObject(sourceJsonStr);
            Object targetJson = JSON.parseObject(targetJsonStr);
            targetJson = syncJsonBodyDiff(isDeleteRedundantParam, sourceJson, targetJson);
            targetBody.setJsonValue(JSON.toJSONString(targetJson));
        } catch (Exception e) {
            LogUtils.info("同步参数 json 解析异常，json1: {}, json2: {}", sourceJsonStr, targetJsonStr);
            // todo 处理非法 json
        }
        return targetBody;
    }

    /**
     * 同步 xml 参数
     * @param isDeleteRedundantParam
     * @param sourceBody
     * @param targetBody
     * @return
     */
    public static XmlBody syncXmlBodyDiff(boolean isDeleteRedundantParam, XmlBody sourceBody, XmlBody targetBody) {
        if (sourceBody == null) {
            return isDeleteRedundantParam ? new XmlBody() : targetBody;
        }
        if (targetBody == null) {
            return sourceBody;
        }

        String sourceXmlStr = sourceBody.getValue();
        String targetXmlStr = targetBody.getValue();
        if (StringUtils.isBlank(sourceXmlStr)) {
            return isDeleteRedundantParam ? new XmlBody() : targetBody;
        }
        if (StringUtils.isBlank(targetXmlStr)) {
            return sourceBody;
        }

        try {
            Element sourceElement = XMLUtils.stringToDocument(sourceXmlStr).getRootElement();
            Element targetElement = XMLUtils.stringToDocument(targetXmlStr).getRootElement();
            targetElement = syncXmlBodyDiff(isDeleteRedundantParam, sourceElement, targetElement);
            String string = parseElementToString(targetElement);
            targetBody.setValue(string);
        } catch (Exception e) {
            LogUtils.info("同步参数 xml 解析异常，xml1: {}, xml2: {}", sourceXmlStr, targetXmlStr);
        }
        return targetBody;
    }

    public static String parseElementToString(Element element) throws IOException {
        StringWriter stringWriter = new StringWriter();
        XMLWriter writer = new XMLWriter(stringWriter);
        writer.write(element);
        return stringWriter.toString();
    }

    /**
     * 同步 xml 参数
     * @param isDeleteRedundantParam
     * @param source
     * @param target
     * @return
     */
    public static Element syncXmlBodyDiff(boolean isDeleteRedundantParam, Element source, Element target) {
        if (source == null) {
            return isDeleteRedundantParam ? null : target.createCopy();
        }
        if (target == null) {
            return source.createCopy();
        }

        List<Element> sourceElements = source.elements();
        List<Element> targetElements = target.elements();

        Map<String, Element> sourceElementMap = sourceElements.stream().collect(Collectors.toMap(Element::getName, Function.identity()));
        Map<String, Element> targetElementMap = targetElements.stream().collect(Collectors.toMap(Element::getName, Function.identity()));

        // 删除多余参数
        if (isDeleteRedundantParam) {
            Iterator<Element> iterator = targetElements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                if (!sourceElementMap.keySet().contains(element.getName())) {
                    iterator.remove();
                }
            }
        }

        for (int i = 0; i < sourceElements.size(); i++) {
            Element sourceElement = sourceElements.get(i);
            Element targetElement = targetElementMap.get(sourceElement.getName());
            if (targetElement == null) {
                // 添加新增参数
                target.add(sourceElement.createCopy());
            } else {
                int index = targetElements.indexOf(targetElement);
                targetElement = syncXmlBodyDiff(isDeleteRedundantParam, sourceElement, targetElement);
                targetElements.set(index, targetElement);
            }
        }
        return target.createCopy();
    }


    /**
     * 同步 json 参数
     * @param sourceJson
     * @param targetJson
     * @return
     */
    public static Object syncJsonBodyDiff(boolean isDeleteRedundantParam, Object sourceJson, Object targetJson) {
        if (sourceJson == null) {
            return isDeleteRedundantParam ? null : targetJson;
        }
        if (targetJson == null) {
            return sourceJson;
        }
        if (sourceJson.getClass() != targetJson.getClass()) {
            return sourceJson;
        }
        if (sourceJson instanceof Map sourceMap && targetJson instanceof Map targetMap) {
            // 删除多余参数
            if (isDeleteRedundantParam) {
                Iterator iterator = targetMap.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    if (!sourceMap.keySet().contains(key)) {
                        iterator.remove();
                    }
                }
            }

            sourceMap.forEach((key, sourceValue) -> {
                if (!targetMap.keySet().contains(key)) {
                    targetMap.put(key, sourceValue);
                    // 添加新增参数
                } else {
                    Object targetValue = targetMap.get(key);
                    targetMap.put(key, syncJsonBodyDiff(isDeleteRedundantParam, sourceValue, targetValue));
                }
            });
        } else if (sourceJson instanceof List souceList && targetJson instanceof List targetList) {
            int size = Math.min(souceList.size(), targetList.size());
            for (int i = 0; i < size; i++) {
                Object sourceValue = souceList.get(i);
                Object targetValue = targetList.get(i);
                targetList.set(i, syncJsonBodyDiff(isDeleteRedundantParam, sourceValue, targetValue));
            }
        }
        return targetJson;
    }


    /**
     * 同步键值对参数
     * @param deleteRedundantParam
     * @param sourceParams
     * @param targetParams
     * @return
     * @param <T>
     */
    public static <T extends KeyValueParam> List<T> syncKeyValueParamDiff(
            boolean deleteRedundantParam, List<T> sourceParams, List<T> targetParams) {
        if (sourceParams == null) {
            return deleteRedundantParam ? new ArrayList<>(0) : targetParams;
        }
        if (targetParams == null) {
            return sourceParams;
        }
        Map<String, ? extends KeyValueParam> sourceMaps = sourceParams.stream()
                .filter(KeyValueParam::isValid)
                .collect(Collectors.toMap(KeyValueParam::getKey, Function.identity()));

        // 删除多余参数
        Iterator<? extends KeyValueParam> iterator = targetParams.iterator();
        if (deleteRedundantParam) {
            while (iterator.hasNext()) {
                KeyValueParam targetParam = iterator.next();
                if (targetParam.isValid() && !sourceMaps.keySet().contains(targetParam.getKey())) {
                    iterator.remove();
                }
            }
        }

        Set<String> targetKeys = targetParams.stream()
                .filter(KeyValueParam::isValid)
                .map(KeyValueParam::getKey)
                .collect(Collectors.toSet());

        // 记住最后一个有效的参数下标
        int lastIndex = targetParams.size() - 1;
        for (int i = targetParams.size() - 1; i >= 0; i--) {
            if (targetParams.get(i).isValid()) {
                lastIndex = i;
                break;
            }
        }

        for (int i = sourceParams.size() - 1; i >= 0; i--) {
            KeyValueParam sourceParam = sourceParams.get(i);
            if (!sourceParam.isValid()) {
                continue;
            }
            if (!targetKeys.contains(sourceParam.getKey())) {
                // 如果不包含则添加
                targetParams.add(lastIndex + 1, (T) sourceParam);
            }
        }

        return targetParams;
    }

}
