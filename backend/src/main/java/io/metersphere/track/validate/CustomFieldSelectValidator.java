package io.metersphere.track.validate;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldOption;
import io.metersphere.excel.constants.TestCaseImportFiled;
import io.metersphere.i18n.Translator;
import io.metersphere.track.constants.TestCaseStatus;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
        prepareCatch(customField);
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
    protected void prepareCatch(CustomFieldDao customField) {
        if (optionValueSetCache.get(customField.getId()) == null) {
            List<CustomFieldOption> options = getOptions(customField.getId(), customField.getOptions());

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
    private void translateSystemOption(CustomFieldDao customField, List<CustomFieldOption> options) {
        Map<String, String> fieldI18nMap = i18nMap.get(customField.getName());
        if (fieldI18nMap != null) {
            // 不为空，说明需要翻译
            Iterator<CustomFieldOption> iterator = options.iterator();
            // 先将系统字段删掉
            while (iterator.hasNext()) {
                CustomFieldOption option = iterator.next();
                if (option.getSystem()) {
                    iterator.remove();
                }
            }
            // 再填充翻译后的值
            for (String optionValue : fieldI18nMap.keySet()) {
                CustomFieldOption option = new CustomFieldOption(optionValue, fieldI18nMap.get(optionValue), true);
                options.add(option);
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

    protected List<CustomFieldOption> getOptions(String id, String optionsStr) {
        List<CustomFieldOption> options = optionCache.get(id);
        if (options != null) {
            return options;
        }
        try {
            return JSONArray.parseArray(optionsStr, CustomFieldOption.class);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return new ArrayList<>();
    }
}
