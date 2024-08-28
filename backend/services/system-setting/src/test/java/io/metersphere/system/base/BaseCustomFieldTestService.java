package io.metersphere.system.base;

import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.constants.InternalUser;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.dto.CustomFieldDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldOptionRequest;
import io.metersphere.system.resolver.field.AbstractCustomFieldResolver;
import io.metersphere.system.resolver.field.CustomFieldResolverFactory;
import io.metersphere.system.service.OrganizationCustomFieldService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.metersphere.system.controller.handler.result.CommonResultCode.FIELD_VALIDATE_ERROR;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-20  11:32
 */
@Service
public class BaseCustomFieldTestService {


    @Resource
    private OrganizationCustomFieldService organizationCustomFieldService;

    private static Map<CustomFieldType, CustomField> customFields;
    
    public static final String OPTION_VALUE = "OPTION_VALUE";


    public Map<CustomFieldType, CustomField> addOrgTestCustomFields() {
        if (customFields != null) {
            return customFields;
        }
        customFields = new HashMap<>();
        for (CustomFieldType customFieldType : CustomFieldType.values()) {
            CustomFieldOptionRequest customFieldOptionRequest = new CustomFieldOptionRequest();
            customFieldOptionRequest.setValue(OPTION_VALUE);
            customFieldOptionRequest.setText("test");
            customFieldOptionRequest.setPos(1);
            List<CustomFieldOptionRequest> optionRequests = Arrays.asList(customFieldOptionRequest);
            CustomField customField = new CustomField();
            customField.setType(customFieldType.name());
            customField.setName(customFieldType.name());
            customField.setCreateUser(InternalUser.ADMIN.getValue());
            customField.setScene(TemplateScene.FUNCTIONAL.name());
            customField.setScopeId(BaseTest.DEFAULT_ORGANIZATION_ID);
            customField = organizationCustomFieldService.add(customField, optionRequests);
            customFields.put(customFieldType, customField);
        }
        return customFields;
    }

    public Map<CustomFieldType, CustomField> getCustomFields() {
        return customFields;
    }

    public void testResolverEmptyValidate() {
        Map<CustomFieldType, Object> emptyValueMap = new HashMap<>();
        Arrays.stream(CustomFieldType.values()).forEach(i -> emptyValueMap.put(i, StringUtils.EMPTY));
        emptyValueMap.put(CustomFieldType.MULTIPLE_SELECT, List.of());
        emptyValueMap.put(CustomFieldType.MULTIPLE_INPUT, List.of());
        emptyValueMap.put(CustomFieldType.CHECKBOX, List.of());
        emptyValueMap.put(CustomFieldType.MULTIPLE_MEMBER, List.of());
        emptyValueMap.put(CustomFieldType.INT, null);
        emptyValueMap.put(CustomFieldType.FLOAT, null);
        emptyValueMap.put(CustomFieldType.SELECT, StringUtils.EMPTY);
        assertValidateError(emptyValueMap);
    }

    public void testResolverErrorValidate() {
        Map<CustomFieldType, Object> errorValueMap = new HashMap<>();
        Arrays.stream(CustomFieldType.values()).forEach(i -> errorValueMap.put(i, 1));
        errorValueMap.put(CustomFieldType.MULTIPLE_SELECT, StringUtils.EMPTY);
        errorValueMap.put(CustomFieldType.MULTIPLE_INPUT, StringUtils.EMPTY);
        errorValueMap.put(CustomFieldType.CHECKBOX, StringUtils.EMPTY);
        errorValueMap.put(CustomFieldType.MULTIPLE_MEMBER, StringUtils.EMPTY);
        errorValueMap.put(CustomFieldType.INT, StringUtils.EMPTY);
        errorValueMap.put(CustomFieldType.FLOAT, StringUtils.EMPTY);
        assertValidateError(errorValueMap);

        errorValueMap.clear();
        Arrays.stream(CustomFieldType.values()).forEach(i -> errorValueMap.put(i, 1));
        errorValueMap.put(CustomFieldType.DATE, "eeee");
        errorValueMap.put(CustomFieldType.DATETIME, "eeee");
        errorValueMap.put(CustomFieldType.DATETIME, "eeee");
        errorValueMap.put(CustomFieldType.MULTIPLE_SELECT, List.of(1));
        errorValueMap.put(CustomFieldType.MULTIPLE_INPUT, List.of(1));
        errorValueMap.put(CustomFieldType.CHECKBOX, List.of(1));
        errorValueMap.put(CustomFieldType.MULTIPLE_MEMBER, List.of(1));
        errorValueMap.put(CustomFieldType.INT, "dd");
        errorValueMap.put(CustomFieldType.FLOAT, "dd");
        assertValidateError(errorValueMap);

        errorValueMap.clear();
        errorValueMap.put(CustomFieldType.MULTIPLE_SELECT, "1");
        errorValueMap.put(CustomFieldType.SELECT, "1");
    }

    public void testResolverParse() {
        Map<CustomFieldType, Object> objectValueMap = new HashMap<>();
        Arrays.stream(CustomFieldType.values()).forEach(i -> objectValueMap.put(i, "1"));
        objectValueMap.put(CustomFieldType.SELECT, OPTION_VALUE);
        objectValueMap.put(CustomFieldType.RADIO, OPTION_VALUE);
        objectValueMap.put(CustomFieldType.DATE, "2021-10-10");
        objectValueMap.put(CustomFieldType.DATETIME, "2021-10-10 10:10:10");
        objectValueMap.put(CustomFieldType.RADIO, OPTION_VALUE);
        objectValueMap.put(CustomFieldType.MULTIPLE_SELECT, List.of(OPTION_VALUE));
        objectValueMap.put(CustomFieldType.CHECKBOX, List.of(OPTION_VALUE));
        objectValueMap.put(CustomFieldType.MULTIPLE_INPUT, List.of(OPTION_VALUE));
        objectValueMap.put(CustomFieldType.MULTIPLE_MEMBER, List.of(OPTION_VALUE));
        objectValueMap.put(CustomFieldType.INT, 1);
        objectValueMap.put(CustomFieldType.FLOAT, 1.2f);

        for (CustomFieldType customFieldType : CustomFieldType.values()) {
            // 校验 validate 调用成功
            invokeValidate(objectValueMap, customFieldType);

            AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customFieldType.name());
            CustomField customField = getCustomFields().get(customFieldType);
            CustomFieldDTO customFieldDTO = BeanUtils.copyBean(new CustomFieldDTO(), customField);
            customFieldDTO.setRequired(true);
            String valueStr = customFieldResolver.parse2String(objectValueMap.get(customFieldType));
            Object objectValue = customFieldResolver.parse2Value(valueStr);
            // 校验 parse2String 和 parse2Value 是否正确
            if (objectValue instanceof List) {
                Assertions.assertEquals(objectValue.toString(), objectValueMap.get(customFieldType).toString());
            } else {
                Assertions.assertEquals(objectValue, objectValueMap.get(customFieldType));
            }
        }
    }

    private void assertValidateError(Map<CustomFieldType, Object> errorValueMap) {
        for (CustomFieldType customFieldType : CustomFieldType.values()) {
            try {
                invokeValidate(errorValueMap, customFieldType);
            } catch (MSException e) {
                Assertions.assertEquals(e.getErrorCode(), FIELD_VALIDATE_ERROR);
                continue;
            }
            throw new MSException("自定义字段校验失败");
        }
    }

    private void invokeValidate(Map<CustomFieldType, Object> valueMap, CustomFieldType customFieldType) {
        AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customFieldType.name());
        CustomField customField = getCustomFields().get(customFieldType);
        CustomFieldDTO customFieldDTO = BeanUtils.copyBean(new CustomFieldDTO(), customField);
        customFieldDTO.setRequired(true);
        customFieldResolver.validate(customFieldDTO, valueMap.get(customFieldType));
    }
}
