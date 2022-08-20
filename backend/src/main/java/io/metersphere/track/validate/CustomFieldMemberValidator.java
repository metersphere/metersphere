package io.metersphere.track.validate;

import io.metersphere.base.domain.User;
import io.metersphere.commons.exception.CustomFieldValidateException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.i18n.Translator;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomFieldMemberValidator extends AbstractCustomFieldValidator {

    protected Map<String, String> userIdMap;
    protected Map<String, String> userNameMap;

    public CustomFieldMemberValidator() {
        this.isKVOption = true;
        UserService userService = CommonBeanFactory.getBean(UserService.class);
        List<User> memberOption = userService.getProjectMemberOption(SessionUtils.getCurrentProjectId());
        userIdMap = memberOption.stream().collect(Collectors.toMap(User::getId, User::getName));
        userNameMap = memberOption.stream().collect(Collectors.toMap(User::getName, User::getId));
    }

    @Override
    public void validate(CustomFieldDao customField, String value) throws CustomFieldValidateException {
        validateRequired(customField, value);
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (userIdMap.containsKey(value) || userNameMap.containsKey(value)) {
            return;
        }
        throw new CustomFieldValidateException(String.format(Translator.get("custom_field_member_tip"), customField.getName()));
    }

    @Override
    public String parse2Key(String keyOrValue, CustomFieldDao customField) {
        if (userNameMap.containsKey(keyOrValue)) {
            return userNameMap.get(keyOrValue);
        }
        return keyOrValue;
    }
}
