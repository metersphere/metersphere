package io.metersphere.system.controller;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTemplateUtilsTests {
    @Resource
    protected CustomFieldMapper customFieldMapper;

    @Test
    @Order(1)
    void getDomainTemplateFieldSuccess() {
        Map<String, String> taskTypeMap = MessageTemplateUtils.getTaskTypeMap();
        Map<String, String> eventMap = MessageTemplateUtils.getEventMap();
        Set<String> typeList = taskTypeMap.keySet();
        Map<String, String> defaultTemplateSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        Assertions.assertTrue(MapUtils.isNotEmpty(defaultTemplateSubjectMap));
        Map<String, String> moduleMap = MessageTemplateUtils.getModuleMap();
        Assertions.assertTrue(MapUtils.isNotEmpty(moduleMap));
        List<String> defaultRelatedUser = MessageTemplateUtils.getDefaultRelatedUser();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(defaultRelatedUser));
        Map<String, String> defaultRelatedUserMap = MessageTemplateUtils.getDefaultRelatedUserMap();
        Assertions.assertTrue(MapUtils.isNotEmpty(defaultRelatedUserMap));
        Map<String, String> fieldSourceMap = MessageTemplateUtils.getFieldSourceMap();
        Assertions.assertTrue(MapUtils.isNotEmpty(fieldSourceMap));
        Map<String, List<CustomField>> customFielddMap = getCustomFielddMap("100001100001");
        for (String type : typeList) {
            Field[] domainTemplateFields = MessageTemplateUtils.getDomainTemplateFields(type);
            Assertions.assertNotNull(domainTemplateFields);
            Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
            eventMap.forEach((event, name) -> {
                String template = defaultTemplateMap.get(type + "_" + event);
                if (StringUtils.isNotBlank(template)) {
                    String translateTemplate = MessageTemplateUtils.getTranslateTemplate(type, template, customFielddMap);
                    Assertions.assertTrue(StringUtils.isNotBlank(translateTemplate));
                    String translateSubject = MessageTemplateUtils.getTranslateSubject(type, template, customFielddMap);
                    Assertions.assertTrue(StringUtils.isNotBlank(translateSubject));
                }
            });

        }

    }

    /**
     * 获取自定义字段的解释
     *
     * @return Map<String, String>
     */
    public Map<String, List<CustomField>> getCustomFielddMap(String projectId) {
        List<String> sceneList = new ArrayList<>();
        sceneList.add(TemplateScene.API.toString());
        sceneList.add(TemplateScene.TEST_PLAN.toString());
        sceneList.add(TemplateScene.FUNCTIONAL.toString());
        sceneList.add(TemplateScene.BUG.toString());
        sceneList.add(TemplateScene.UI.toString());
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andScopeIdEqualTo(projectId).andSceneIn(sceneList);
        List<CustomField> customFields = customFieldMapper.selectByExample(example);
        return customFields.stream().collect(Collectors.groupingBy(CustomField::getScene));
    }
}
