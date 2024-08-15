package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.JsonBody;
import io.metersphere.jmeter.functions.MockFunction;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-14  21:15
 */
public class MsJsonBodyConverter extends MsBodyConverter<JsonBody> {
    @Override
    public String parse(HTTPSamplerProxy sampler, JsonBody body, ParameterConfig config) {
        sampler.setPostBodyRaw(true);
        try {
            String raw = parseJsonMock(body.getJsonValue());
            handleRowBody(sampler, raw);
        } catch (Exception e) {
            LogUtils.error("json mock value is abnormal", e);
        }
        return MediaType.APPLICATION_JSON_VALUE;
    }


    /**
     * 将json中的 @xxx 转换成 ${__Mock(@xxx)}
     *
     * @param jsonStr
     * @return
     */
    private String parseJsonMock(String jsonStr) {
        if (StringUtils.isNotEmpty(jsonStr)) {
            String value = StringUtils.chomp(jsonStr.trim());
            try {
                if (StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) {
                    List list = JSON.parseArray(jsonStr);
                    parseMock(list);
                    return replaceMockComma(JSON.toJSONString(list));
                } else {
                    Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
                    parseMock(map);
                    return replaceMockComma(JSON.toJSONString(map));
                }
            } catch (Exception e) {
                // 如果json串中的格式不是标准的json格式，正则替换变量
                return parseTextMock(jsonStr);
            }
        }
        return jsonStr;
    }

    /**
     * 将文本中的 @xxx 转换成 ${__Mock(@xxx)}
     *
     * @param text
     * @return
     */
    protected String parseTextMock(String text) {
        String pattern = "[\"\\s:]@[a-zA-Z\\\\(|,'-\\\\d ]*[a-zA-Z)-9),\\\\\"]";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        while (matcher.find()) {
            //取出group的最后一个字符 主要是防止 @string|number 和 @string 这种情况
            //如果是 “ 或者, 结尾的  需要截取
            String group = matcher.group();
            if (group.endsWith(",") || group.endsWith("\"")) {
                group = group.substring(0, group.length() - 1);
            }
            // 去掉第一个字符，因为第一个字符是 " : 或者空格
            group = group.substring(1, group.length());
            text = text.replace(group, StringUtils.join("${__Mock(", group.replace(",", "\\,"), ")}"));
        }
        return text;
    }

    private void parseMock(List list) {
        Map<Integer, String> replaceDataMap = new HashMap<>();
        for (int index = 0; index < list.size(); index++) {
            Object obj = list.get(index);
            if (obj instanceof Map) {
                parseMock((Map) obj);
            } else if (obj instanceof String) {
                if (StringUtils.isNotBlank((String) obj)) {
                    String str = buildFunctionCallString((String) obj);
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

    private void parseMock(Map map) {
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof List) {
                parseMock((List) value);
            } else if (value instanceof Map) {
                parseMock((Map) value);
            } else if (value instanceof String) {
                if (StringUtils.isNotBlank((String) value)) {
                    value = buildFunctionCallString((String) value);
                }
                map.put(key, value);
            }
        }
    }

    /**
     * ${__Mock(@integer(1,100))} -> ${__Mock(@integer(1\,100))}
     * 将 mock 函数中的逗号转义，让 jmeter 识别
     *
     * @param text
     * @return
     */
    protected String replaceMockComma(String text) {
        String pattern = "\\$\\{__Mock\\((.+?)\\)\\}";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        while (matcher.find()) {
            String group = matcher.group();
            if (StringUtils.contains(group, ",")) {
                text = text.replace(group, group.replace(",", "\\,"));
            }
        }
        return text;
    }

    /**
     * 将文本中的 @xxx 转换成 ${__Mock(@xxx)}
     * 这里不使用 Mock.buildFunctionCallString 的逗号转义
     *
     * @param input
     * @return
     */
    public static String buildFunctionCallString(String input) {
        if (!StringUtils.startsWith(input, "@")) {
            return input;
        }
        return String.format("${%s(%s)}", MockFunction.KEY, input);
    }
}
