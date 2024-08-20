package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wx
 */
public class CustomFieldMemberValidator extends AbstractCustomFieldValidator {

    protected Map<String, String> userIdMap;
    protected Map<String, String> userEmailMap;
    protected Map<String, String> userIdEmailMap;

    public CustomFieldMemberValidator() {
    }

    public CustomFieldMemberValidator(String projectId) {
        this.isKVOption = true;
        ProjectApplicationService projectApplicationService = CommonBeanFactory.getBean(ProjectApplicationService.class);
        List<User> memberOption = projectApplicationService.getProjectUserList(projectId);
        userIdMap = memberOption.stream()
                .collect(
                        Collectors.toMap(user -> user.getId().toLowerCase(), User::getId)
                );
        userEmailMap = new HashMap<>();
        memberOption.stream()
                .forEach(user -> userEmailMap.put(user.getEmail().toLowerCase(), user.getId()));
        userIdEmailMap = new HashMap<>();
        memberOption.stream()
                .forEach(user -> userIdEmailMap.put(user.getId(), user.getEmail().toLowerCase()));
    }

    @Override
    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }
        value = value.toLowerCase();
        if (userIdMap.containsKey(value) || userEmailMap.containsKey(value)) {
            return;
        }
        throw new CustomFieldValidateException(String.format(Translator.get("custom_field_member_tip"), customField.getFieldName()));
    }

    @Override
    public Object parse2Key(String keyOrValue, TemplateCustomFieldDTO customField) {
        keyOrValue = keyOrValue.toLowerCase();
        if (userIdMap.containsKey(keyOrValue)) {
            return userIdMap.get(keyOrValue);
        }
        if (userEmailMap.containsKey(keyOrValue)) {
            return userEmailMap.get(keyOrValue);
        }
        return keyOrValue;
    }

    @Override
    public Object parse2Value(String keyOrValue, TemplateCustomFieldDTO customField) {
        keyOrValue = keyOrValue.toLowerCase();
        if (userIdEmailMap.containsKey(keyOrValue)) {
            return userIdEmailMap.get(keyOrValue);
        }
        return keyOrValue;
    }

}
