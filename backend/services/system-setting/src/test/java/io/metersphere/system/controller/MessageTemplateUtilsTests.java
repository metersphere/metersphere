package io.metersphere.system.controller;

import io.metersphere.system.notice.utils.MessageTemplateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTemplateUtilsTests {

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
        for (String type : typeList) {
            Field[] domainTemplateFields = MessageTemplateUtils.getDomainTemplateFields(type);
            Assertions.assertNotNull(domainTemplateFields);
            Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
            eventMap.forEach((event, name) -> {
                String template = defaultTemplateMap.get(type + "_" + event);
                if (StringUtils.isNotBlank(template)) {
                    String translateTemplate = MessageTemplateUtils.getTranslateTemplate(type, template);
                    Assertions.assertTrue(StringUtils.isNotBlank(translateTemplate));
                }
            });

        }

    }
}
