package io.metersphere.functional.excel.validate;


import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.utils.SessionUtils;
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
    protected Map<String, String> userNameMap;

    public CustomFieldMemberValidator() {
        this.isKVOption = true;
        ProjectApplicationService projectApplicationService = CommonBeanFactory.getBean(ProjectApplicationService.class);
        List<User> memberOption = projectApplicationService.getProjectUserList(SessionUtils.getCurrentProjectId());
        userIdMap = memberOption.stream()
                .collect(
                        Collectors.toMap(user -> user.getId().toLowerCase(), User::getId)
                );
        userNameMap = new HashMap<>();
        memberOption.stream()
                .forEach(user -> userNameMap.put(user.getName().toLowerCase(), user.getId()));
    }

    @Override
    public void validate(TemplateCustomFieldDTO customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }
        value = value.toLowerCase();
        if (userIdMap.containsKey(value) || userNameMap.containsKey(value)) {
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
        if (userNameMap.containsKey(keyOrValue)) {
            return userNameMap.get(keyOrValue);
        }
        return keyOrValue;
    }
}
