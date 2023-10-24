package io.metersphere.system.resolver.field;


import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.CustomFieldDao;
import io.metersphere.system.service.BaseCustomFieldOptionService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomFieldSelectResolver extends AbstractCustomFieldResolver {

    @Override
    public void validate(CustomFieldDao customField, Object value) {
        validateRequired(customField, value);
        if (value == null) {
            return;
        }
        validateString(customField.getName(), value);
        List<CustomFieldOption> options = getOptions(customField.getId());
        Set<String> values = options.stream().map(CustomFieldOption::getValue).collect(Collectors.toSet());
        if (!values.contains(value)) {
            throwValidateException(customField.getName());
        }
    }

    protected List<CustomFieldOption> getOptions(String id) {
        BaseCustomFieldOptionService customFieldOptionService = CommonBeanFactory.getBean(BaseCustomFieldOptionService.class);
        return customFieldOptionService.getByFieldId(id);
    }
}
