package io.metersphere.track.validate;

import io.metersphere.commons.constants.CustomFieldType;

import java.util.HashMap;

public class CustomFieldValidatorFactory {

    public static HashMap<String, AbstractCustomFieldValidator> getValidatorMap() {
        return new HashMap<>() {{
            put(CustomFieldType.SELECT.getValue(), new CustomFieldSelectValidator());
            put(CustomFieldType.RADIO.getValue(), new CustomFieldSelectValidator());

            put(CustomFieldType.MULTIPLE_SELECT.getValue(), new CustomFieldMultipleSelectValidator());
            put(CustomFieldType.CHECKBOX.getValue(), new CustomFieldMultipleSelectValidator());

            put(CustomFieldType.INPUT.getValue(), new CustomFieldTextValidator());
            put(CustomFieldType.RICH_TEXT.getValue(), new CustomFieldTextValidator());
            put(CustomFieldType.TEXTAREA.getValue(), new CustomFieldTextValidator());

            put(CustomFieldType.MULTIPLE_INPUT.getValue(), new CustomFieldMultipleTextValidator());

            put(CustomFieldType.DATE.getValue(), new CustomFieldDateValidator());
            put(CustomFieldType.DATETIME.getValue(), new CustomFieldDateTimeValidator());

            put(CustomFieldType.MEMBER.getValue(), new CustomFieldMemberValidator());
            put(CustomFieldType.MULTIPLE_MEMBER.getValue(), new CustomFieldMultipleMemberValidator());

            put(CustomFieldType.INT.getValue(), new CustomFieldIntegerValidator());
            put(CustomFieldType.FLOAT.getValue(), new CustomFieldFloatValidator());
        }};
    }
}
