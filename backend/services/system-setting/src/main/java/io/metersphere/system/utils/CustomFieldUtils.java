package io.metersphere.system.utils;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.BaseCondition;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义字段处理工具类
 */
@UtilityClass
public class CustomFieldUtils {

    public static final String COMBINE_CUSTOM = "customs";
    public static final String COMBINE_CUSTOM_FIELD_TYPE = "type";
    public static final String COMBINE_CUSTOM_FIELD_VALUE = "value";
    public static final String COMBINE_CUSTOM_FIELD_OPERATOR = "operator";
    public static final String IS_CURRENT_USER = "current user";
    public static final String CUSTOM_MULTIPLE_PREFIX = "custom_multiple";

    /**
     * 设置列表查询的多选字段参数
     * @param request 请求参数
     */
    public static void setBaseQueryRequestCustomMultipleFields(BaseCondition request, String userId) {
        handleFilterCustomMultipleFields(request);
        handleCombineFields(request, userId);
    }

    private static void handleFilterCustomMultipleFields(BaseCondition request) {
        if (MapUtils.isNotEmpty(request.getFilter())) {
            request.getFilter().forEach((key, value) -> {
                if (key.startsWith(CUSTOM_MULTIPLE_PREFIX) && CollectionUtils.isNotEmpty(value)) {
                    List<String> jsonValues = value.stream().map(item -> "[\"".concat(item).concat("\"]")).toList();
                    request.getFilter().put(key, jsonValues);
                }
            });
        }
    }

    private static void handleCombineFields(BaseCondition request, String userId) {
        Map<String, Object> combine = request.getCombine();
        if (MapUtils.isNotEmpty(combine)) {
            combine.forEach((k, v) -> {
                if (StringUtils.equals(k, COMBINE_CUSTOM) && ObjectUtils.isNotEmpty(v)) {
                    handleCombineCustomFields((List<Map<String, Object>>) v, userId);
                } else {
                    handleCombineField(v, userId);
                }
            });
        }
    }

    private static void handleCombineCustomFields(List<Map<String, Object>> customs, String userId) {
        customs.forEach(custom -> {
            String operator = custom.get(COMBINE_CUSTOM_FIELD_OPERATOR).toString();
            if (StringUtils.equalsIgnoreCase(operator, IS_CURRENT_USER)) {
                custom.put(COMBINE_CUSTOM_FIELD_VALUE, userId);
                return;
            }
            String fieldType = custom.get(COMBINE_CUSTOM_FIELD_TYPE).toString();
            String fieldValue = custom.get(COMBINE_CUSTOM_FIELD_VALUE).toString();

            if (StringUtils.equalsAny(fieldType, CustomFieldType.MULTIPLE_MEMBER.name(),
                    CustomFieldType.CHECKBOX.name(), CustomFieldType.MULTIPLE_SELECT.name()) && StringUtils.isNotEmpty(fieldValue)) {
                List<String> customValues = JSON.parseArray(fieldValue, String.class);
                List<String> jsonValues = customValues.stream().map(item -> "JSON_CONTAINS(`value`, '[\"".concat(item).concat("\"]')")).toList();
                custom.put(COMBINE_CUSTOM_FIELD_VALUE, "(".concat(StringUtils.join(jsonValues, " OR ")).concat(")"));
            }
        });
    }

    private static void handleCombineField(Object v, String userId) {
        Map<String, Object> combineField = new HashMap<>((Map<String, Object>) v);
        if (StringUtils.equalsIgnoreCase(combineField.get(COMBINE_CUSTOM_FIELD_OPERATOR).toString(), IS_CURRENT_USER)) {
            combineField.put(COMBINE_CUSTOM_FIELD_VALUE, userId);
        }
    }


    /**
     * 多选字段追加值
     * @param originalVal 原始值
     * @param appendVal 追加值
     * @return 追加后的值
     */
    public static String appendToMultipleCustomField(String originalVal, String appendVal) {
        if (StringUtils.isEmpty(originalVal)) {
            return appendVal;
        }
        List<String> originalList = JSON.parseArray(originalVal, String.class);
        List<String> appendList = JSON.parseArray(appendVal, String.class);
        originalList.addAll(appendList);
        // 追加后需去重
        return JSON.toJSONString(originalList.stream().distinct().toList());
    }
}
