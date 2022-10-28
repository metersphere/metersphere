package io.metersphere.validate;

import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.CustomFieldOptionDTO;
import io.metersphere.excel.constants.TestCaseImportFiled;
import io.metersphere.exception.CustomFieldValidateException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import io.metersphere.constants.TestCaseStatus;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CustomFieldSelectValidator extends AbstractCustomFieldValidator {

    /**
     * 缓存每个字段对应的选项值
     */
    Map<String, List<CustomFieldOptionDTO>> optionCache = new HashMap<>();
    Map<String, Set<String>> optionValueSetCache = new HashMap<>();
    Map<String, Set<String>> optionTextSetCache = new HashMap<>();
    Map<String, Map<String, String>> optionTextMapCache = new HashMap<>();

    /**
     * 保存系统字段中选项翻译后的值
     * key 为字段名称，value 为选项value，和选项值的映射
     */
    Map<String, Map<String, String>> i18nMap = new HashMap<>();

    public CustomFieldSelectValidator() {
        Map<String, String> statusI18nMap = new HashMap<>();
        for (TestCaseStatus status : TestCaseStatus.values()) {
            statusI18nMap.put(status.name(), Translator.get(status.getI18nKey()));
        }
        i18nMap.put(TestCaseImportFiled.STATUS.getFiledLangMap().get(Locale.SIMPLIFIED_CHINESE), statusI18nMap);
        this.isKVOption = true;
    }

    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }
        prepareCache(customField);
        Set<String> idSet = optionValueSetCache.get(customField.getId());
        Set<String> textSet = optionTextSetCache.get(customField.getId());
        if (!idSet.contains(value) && !textSet.contains(value)) {
            CustomFieldValidateException.throwException(String.format(Translator.get("custom_field_select_tip"), customField.getName(), textSet));
        }
    }

    @Override
    public Object parse2Key(String keyOrValuesStr, CustomFieldDao customField) {
        Map<String, String> textMap = optionTextMapCache.get(customField.getId());
        if (MapUtils.isNotEmpty(textMap) && textMap.containsKey(keyOrValuesStr)) {
            return textMap.get(keyOrValuesStr);
        }
        return keyOrValuesStr;
    }

    /**
     * 获取自定义字段的选项值和key
     * 存储到缓存中，增强导入时性能
     *
     * @param customField
     */
    protected void prepareCache(CustomFieldDao customField) {
        if (optionValueSetCache.get(customField.getId()) == null) {
            List<CustomFieldOptionDTO> options = getOptions(customField.getId(), customField.getOptions());

            translateSystemOption(customField, options);

            optionValueSetCache.put(customField.getId(), getIdSet(options));
            optionTextSetCache.put(customField.getId(), getNameSet(options));
            optionTextMapCache.put(customField.getId(), getTextMap(options));
        }
    }

    /**
     * 翻译系统字段的选项名称
     * @param customField
     * @param options
     */
    private void translateSystemOption(CustomFieldDao customField, List<CustomFieldOptionDTO> options) {
        Map<String, String> fieldI18nMap = i18nMap.get(customField.getName());
        // 不为空，说明需要翻译
        if (fieldI18nMap != null) {
            Iterator<CustomFieldOptionDTO> iterator = options.iterator();
            // 替换成翻译后的值
            while (iterator.hasNext()) {
                CustomFieldOptionDTO option = iterator.next();
                if (option.getSystem() && fieldI18nMap.keySet().contains(option.getValue())) {
                    option.setText(fieldI18nMap.get(option.getValue()));
                }
            }
        }
    }

    @NotNull
    private Map<String, String> getTextMap(List<CustomFieldOptionDTO> options) {
        HashMap<String, String> textMap = new HashMap<>();
        options.forEach(item -> textMap.put(item.getText(), item.getValue()));
        return textMap;
    }

    @NotNull
    protected Set<String> getNameSet(List<CustomFieldOptionDTO> options) {
        return options.stream()
                .map(CustomFieldOptionDTO::getText)
                .collect(Collectors.toSet());
    }

    @NotNull
    protected Set<String> getIdSet(List<CustomFieldOptionDTO> options) {
        return options.stream()
                .map(CustomFieldOptionDTO::getValue)
                .collect(Collectors.toSet());
    }

    protected List<CustomFieldOptionDTO> getOptions(String id, String optionsStr) {
        List<CustomFieldOptionDTO> options = optionCache.get(id);
        if (options != null) {
            return options;
        }
        try {
            return JSON.parseArray(optionsStr, CustomFieldOptionDTO.class);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return new ArrayList<>();
    }
}
