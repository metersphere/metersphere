package io.metersphere.system.controller;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.sdk.BaseCondition;
import io.metersphere.system.utils.CustomFieldUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author: LAN
 * @date: 2023/12/14 10:18
 * @version: 1.0
 */

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomFieldTests extends BaseTest {
    @Test
    @Order(1)
    void testSetBaseQueryRequestCustomMultipleFields() {
        BaseCondition baseCondition = new BaseCondition();
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("status", Arrays.asList("Underway", "Completed"));
        filters.put("custom_multiple_custom-field", List.of("oasis"));

        Map<String, Object> map = new HashMap<>();
        map.put("name", Map.of("operator", "like", "value", "test"));
        map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
        map.put("createUser", Map.of("operator", "current user", "value", StringUtils.EMPTY));
        List<Map<String, Object>> customs = new ArrayList<>();
        Map<String, Object> custom = new HashMap<>();
        custom.put("id", "test_field");
        custom.put("operator", "in");
        custom.put("type", "multipleSelect");
        custom.put("value",  JSON.toJSONString(List.of("test", "default")));
        customs.add(custom);
        Map<String, Object> currentUserCustom = new HashMap<>();
        currentUserCustom.put("id", "test_field");
        currentUserCustom.put("operator", "current user");
        currentUserCustom.put("type", "multipleMember");
        currentUserCustom.put("value", "current user");
        customs.add(currentUserCustom);
        currentUserCustom.put("value", "");
        customs.add(currentUserCustom);
        map.put("customs", customs);

        baseCondition.setFilter(filters);
        baseCondition.setCombine(map);

        // 调用测试方法
        CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(baseCondition, "admin");

        // 验证预期结果
        Assertions.assertNotNull(baseCondition.getFilter());
        Assertions.assertNotNull(baseCondition.getCombine());

        // 验证多选字段是否被正确处理
        List<String> customMultipleValues = baseCondition.getFilter().get("custom_multiple_custom-field");
        Assertions.assertNotNull(customMultipleValues);
        Assertions.assertEquals(1, customMultipleValues.size());
        Assertions.assertTrue(customMultipleValues.contains("[\"oasis\"]"));

        // 验证 CombineField 方法
        Map<String, Object> combineField = (Map<String, Object>) baseCondition.getCombine().get("createUser");
        Assertions.assertNotNull(combineField);
        Assertions.assertEquals(StringUtils.EMPTY, combineField.get("value"));
    }

    @Test
    void testHandleFilterCustomMultipleFieldsEmptyFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 创建测试数据
        BaseCondition baseCondition = new BaseCondition();

        // 调用被测试方法
        CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(baseCondition, "user123");

        // 验证预期结果
        Assertions.assertNull(baseCondition.getFilter());
    }

    @Test
    void testHandleCombineFieldsEmptyCombine() {
        // 创建测试数据
        BaseCondition baseCondition = new BaseCondition();

        // 调用被测试方法
        CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(baseCondition, "user123");

        // 验证预期结果
        Assertions.assertNull(baseCondition.getCombine());
    }



    @Test
    @Order(2)
    void testAppendToMultipleCustomFieldWithEmptyOriginalValue() {
        // 创建测试数据，originalValue 为空
        String originalValue = "";
        String appendValue = "[\"value2\",\"value3\"]";

        // 调用被测试方法
        String resultEmptyOriginal = CustomFieldUtils.appendToMultipleCustomField(originalValue, appendValue);

        // 验证预期结果
        Assertions.assertNotNull(resultEmptyOriginal);
        Assertions.assertEquals("[\"value2\",\"value3\"]", resultEmptyOriginal);
    }

    @Test
    @Order(3)
    void testAppendToMultipleCustomField() {
        // 创建测试数据
        String originalValue = "[\"value1\", \"value2\"]";
        String appendValue = "[\"value2\", \"value3\"]";

        // 调用被测试方法
        String result = CustomFieldUtils.appendToMultipleCustomField(originalValue, appendValue);


        // 验证预期结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("[\"value1\",\"value2\",\"value3\"]", result);
    }
}
