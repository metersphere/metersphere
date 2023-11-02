package io.metersphere.bug.utils;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.BasePageRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字段处理工具类
 * @author song-cc-rock
 */
public class CustomFieldUtils {

    public static final String COMBINE_CUSTOM = "customs";
    public static final String COMBINE_CUSTOM_FIELD_TYPE = "type";
    public static final String COMBINE_CUSTOM_FIELD_VALUE = "value";
//    public static final String COMBINE_CUSTOM_FIELD_OPERATOR = "operator";
//    public static final String IS_CURRENT_USER = "current user";
    public static final String CUSTOM_MULTIPLE_PREFIX = "custom_multiple";

    /**
     * 设置列表查询的多选字段参数
     * @param request 请求参数
     */
    public static void setBaseQueryRequestCustomMultipleFields(BasePageRequest request) {
        // handle filter custom multiple fields
        if (MapUtils.isNotEmpty(request.getFilter())) {
            request.getFilter().entrySet().forEach(entry -> {
                if (entry.getKey().startsWith(CUSTOM_MULTIPLE_PREFIX) && CollectionUtils.isNotEmpty(entry.getValue())) {
                    List<String> jsonValues = entry.getValue().stream().map(item -> "[\"".concat(item).concat("\"]")).collect(Collectors.toList());
                    entry.setValue(jsonValues);
                }
            });
        }

        // handle combine custom multiple fields
        if (MapUtils.isNotEmpty(request.getCombine()) && ObjectUtils.isNotEmpty((request.getCombine().get(COMBINE_CUSTOM)))) {
            //noinspection unchecked
            List<Map<String, Object>> customs = (List<Map<String, Object>>) request.getCombine().get(COMBINE_CUSTOM);
            customs.forEach(custom -> {
                // when member select or member multipart select support current user, open it
//                if(StringUtils.equalsIgnoreCase(custom.get(COMBINE_CUSTOM_FIELD_OPERATOR).toString(), IS_CURRENT_USER)){
//                    String userId = SessionUtils.getUserId();
//                    custom.put(COMBINE_CUSTOM_FIELD_VALUE, userId);
//                }
                if (StringUtils.equalsAny(custom.get(COMBINE_CUSTOM_FIELD_TYPE).toString(), CustomFieldType.MULTIPLE_MEMBER.getType(),
                        CustomFieldType.CHECKBOX.getType(), CustomFieldType.MULTIPLE_SELECT.getType())
                        && StringUtils.isNotEmpty(custom.get(COMBINE_CUSTOM_FIELD_VALUE).toString())) {
                    List<String> customValues = JSON.parseArray(custom.get(COMBINE_CUSTOM_FIELD_VALUE).toString(), String.class);
                    List<String> jsonValues = customValues.stream().map(item -> "JSON_CONTAINS(`value`, '[\"".concat(item).concat("\"]')")).toList();
                    custom.put(COMBINE_CUSTOM_FIELD_VALUE, "(".concat(StringUtils.join(jsonValues, " OR ")).concat(")"));
                }
            });
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
        List<String> orignalList = JSON.parseArray(originalVal, String.class);
        List<String> appendList = JSON.parseArray(appendVal, String.class);
        orignalList.addAll(appendList);
        // 追加后需去重
        return JSON.toJSONString(orignalList.stream().distinct().toList());
    }
}
