package io.metersphere.functional.excel.validate;

import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wx
 */
public class CustomFieldSelectValidator extends AbstractCustomFieldValidator {

    /**
     * 缓存每个字段对应的选项值
     */
    Map<String, List<CustomFieldOption>> optionCache = new HashMap<>();
    Map<String, Set<String>> optionValueSetCache = new HashMap<>();
    Map<String, Set<String>> optionTextSetCache = new HashMap<>();
    Map<String, Map<String, String>> optionTextMapCache = new HashMap<>();

    /**
     * 保存系统字段中选项翻译后的值
     * key 为字段名称，value 为选项value，和选项值的映射
     */
    Map<String, Map<String, String>> i18nMap = new HashMap<>();

    public CustomFieldSelectValidator() {
        this.isKVOption = true;
    }

    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }
        prepareCache(customField);
        Set<String> idSet = optionValueSetCache.get(customField.getFieldId());
        Set<String> textSet = optionTextSetCache.get(customField.getFieldId());
        if (customField.getFieldName().equals(Translator.get("custom_field.functional_priority"))) {
            value = value.toUpperCase();
        }
        if (!idSet.contains(value) && !textSet.contains(value)) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_select_tip"), customField.getFieldName(), textSet));
        }
    }

    @Override
    public Object parse2Key(String keyOrValuesStr, TemplateCustomFieldDTO customField) {
        Map<String, String> textMap = optionTextMapCache.get(customField.getFieldId());
        if (MapUtils.isNotEmpty(textMap) && textMap.containsKey(keyOrValuesStr)) {
            return textMap.get(keyOrValuesStr);
        }
        return keyOrValuesStr;
    }

    @Override
    public Object parse2Value(String keyOrValuesStr, TemplateCustomFieldDTO customField) {
        Map<String, String> optionValueMap = customField.getOptions().stream().collect(Collectors.toMap(CustomFieldOption::getValue, CustomFieldOption::getText));
        if (optionValueMap.containsKey(keyOrValuesStr)) {
            return optionValueMap.get(keyOrValuesStr);
        }
        return keyOrValuesStr;
    }

    /**
     * 获取自定义字段的选项值和key
     * 存储到缓存中，增强导入时性能
     *
     * @param customField
     */
    protected void prepareCache(TemplateCustomFieldDTO customField) {
        if (optionValueSetCache.get(customField.getFieldId()) == null) {
            List<CustomFieldOption> options = getOptions(customField.getFieldId(), customField.getOptions());
            translateSystemOption(customField, options);

            optionValueSetCache.put(customField.getFieldId(), getIdSet(options));
            optionTextSetCache.put(customField.getFieldId(), getNameSet(options));
            optionTextMapCache.put(customField.getFieldId(), getTextMap(options));
        }
    }

    /**
     * 翻译系统字段的选项名称
     *
     * @param customField
     * @param options
     */
    private void translateSystemOption(TemplateCustomFieldDTO customField, List<CustomFieldOption> options) {
        Map<String, String> fieldI18nMap = i18nMap.get(customField.getFieldName());
        // 不为空，说明需要翻译
        if (fieldI18nMap != null) {
            Iterator<CustomFieldOption> iterator = options.iterator();
            // 替换成翻译后的值
            while (iterator.hasNext()) {
                CustomFieldOption option = iterator.next();
                if (option.getInternal() && fieldI18nMap.keySet().contains(option.getValue())) {
                    option.setText(fieldI18nMap.get(option.getValue()));
                }
            }
        }
    }

    @NotNull
    private Map<String, String> getTextMap(List<CustomFieldOption> options) {
        HashMap<String, String> textMap = new HashMap<>();
        options.forEach(item -> textMap.put(item.getText(), item.getValue()));
        return textMap;
    }

    @NotNull
    protected Set<String> getNameSet(List<CustomFieldOption> options) {
        return options.stream()
                .map(CustomFieldOption::getText)
                .collect(Collectors.toSet());
    }

    @NotNull
    protected Set<String> getIdSet(List<CustomFieldOption> options) {
        return options.stream()
                .map(CustomFieldOption::getValue)
                .collect(Collectors.toSet());
    }

    protected List<CustomFieldOption> getOptions(String id, List<CustomFieldOption> customFieldOptions) {
        List<CustomFieldOption> options = optionCache.get(id);
        if (options != null) {
            return options;
        }
        try {
            return customFieldOptions;
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return new ArrayList<>();
    }
}
