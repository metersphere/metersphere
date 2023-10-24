package io.metersphere.system.resolver.field;

import io.metersphere.sdk.constants.CustomFieldType;

import java.util.HashMap;

public class  CustomFieldResolverFactory {

    private static final HashMap<String, AbstractCustomFieldResolver> resolverMap = new HashMap<>();

    static {
        resolverMap.put(CustomFieldType.SELECT.name(), new CustomFieldSelectResolver());
        resolverMap.put(CustomFieldType.RADIO.name(), new CustomFieldSelectResolver());

        resolverMap.put(CustomFieldType.MULTIPLE_SELECT.name(), new CustomFieldMultipleSelectResolver());
        resolverMap.put(CustomFieldType.CHECKBOX.name(), new CustomFieldMultipleSelectResolver());

        resolverMap.put(CustomFieldType.INPUT.name(), new CustomFieldTextResolver());
        resolverMap.put(CustomFieldType.TEXTAREA.name(), new CustomFieldTextResolver());

        resolverMap.put(CustomFieldType.MULTIPLE_INPUT.name(), new CustomFieldMultipleTextResolver());

        resolverMap.put(CustomFieldType.DATE.name(), new CustomFieldDateResolver());
        resolverMap.put(CustomFieldType.DATETIME.name(), new CustomFieldDateTimeResolver());

        resolverMap.put(CustomFieldType.MEMBER.name(), new CustomFieldMemberResolver());
        resolverMap.put(CustomFieldType.MULTIPLE_MEMBER.name(), new CustomFieldMultipleMemberResolver());

        resolverMap.put(CustomFieldType.INT.name(), new CustomFieldIntegerResolver());
        resolverMap.put(CustomFieldType.FLOAT.name(), new CustomFieldFloatResolver());
    }

    public static AbstractCustomFieldResolver getResolver(String type) {
        return resolverMap.get(type);
    }
}
