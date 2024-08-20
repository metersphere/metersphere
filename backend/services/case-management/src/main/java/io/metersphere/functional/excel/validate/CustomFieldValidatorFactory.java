package io.metersphere.functional.excel.validate;


import io.metersphere.sdk.constants.CustomFieldType;

import java.util.HashMap;

/**
 * @author wx
 */
public class CustomFieldValidatorFactory {

    private static final HashMap<String, AbstractCustomFieldValidator> validatorMap = new HashMap<>();

    public static HashMap<String, AbstractCustomFieldValidator> getValidatorMap(String projectId) {
        validatorMap.put(CustomFieldType.SELECT.name(), new CustomFieldSelectValidator());
        validatorMap.put(CustomFieldType.SELECT.name(), new CustomFieldSelectValidator());
        validatorMap.put(CustomFieldType.RADIO.name(), new CustomFieldSelectValidator());

        validatorMap.put(CustomFieldType.MULTIPLE_SELECT.name(), new CustomFieldMultipleSelectValidator());
        validatorMap.put(CustomFieldType.CHECKBOX.name(), new CustomFieldMultipleSelectValidator());

        validatorMap.put(CustomFieldType.INPUT.name(), new CustomFieldTextValidator());
        validatorMap.put(CustomFieldType.TEXTAREA.name(), new CustomFieldTextValidator());

        validatorMap.put(CustomFieldType.MULTIPLE_INPUT.name(), new CustomFieldMultipleTextValidator());

        validatorMap.put(CustomFieldType.DATE.name(), new CustomFieldDateValidator());
        validatorMap.put(CustomFieldType.DATETIME.name(), new CustomFieldDateTimeValidator());

        validatorMap.put(CustomFieldType.MEMBER.name(), new CustomFieldMemberValidator(projectId));
        validatorMap.put(CustomFieldType.MULTIPLE_MEMBER.name(), new CustomFieldMultipleMemberValidator(projectId));

        validatorMap.put(CustomFieldType.INT.name(), new CustomFieldIntegerValidator());
        validatorMap.put(CustomFieldType.FLOAT.name(), new CustomFieldFloatValidator());
        return validatorMap;
    }
}
