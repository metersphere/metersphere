package io.metersphere.system.notice.utils;

import io.metersphere.sdk.util.Translator;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageTemplateUtils {

    private static void setFieldNameMap(Field[] moduleFields, Map<String, String> moduleMap) {
        for (Field moduleField : moduleFields) {
            Schema annotation = moduleField.getAnnotation(Schema.class);
            String name = "";
            if (annotation != null) {
                name = Translator.get(annotation.description());
            }
            moduleMap.put(moduleField.getName(), name);
        }
    }

    /**
     * 获取模块翻译后的名称map
     *
     * @return Map<String, String> moduleMap
     */
    public static Map<String, String> getModuleMap() {
        Field[] moduleFields = FieldUtils.getAllFields(NoticeConstants.Module.class);
        Map<String, String> moduleMap = new HashMap<>();
        setFieldNameMap(moduleFields, moduleMap);
        return moduleMap;
    }

    /**
     * 获取任务类型翻译后的名称map
     *
     * @return Map<String, String> taskTypeMap
     */
    public static Map<String, String> getTaskTypeMap() {
        Map<String, String> taskTypeMap = new HashMap<>();
        Field[] taskTypeFields = FieldUtils.getAllFields(NoticeConstants.TaskType.class);
        setFieldNameMap(taskTypeFields, taskTypeMap);
        return taskTypeMap;
    }

    /**
     * 获取事件翻译后的名称map
     *
     * @return Map<String, String> eventMap
     */
    public static Map<String, String> getEventMap() {
        Map<String, String> eventMap = new HashMap<>();
        Field[] eventFields = FieldUtils.getAllFields(NoticeConstants.Event.class);
        setFieldNameMap(eventFields, eventMap);
        return eventMap;
    }

    /**
     * 获取事件的默认模版
     *
     * @return Map<String, String> defaultTemplateMap
     */
    public static Map<String, String> getDefaultTemplateMap() {
        Map<String, String> defaultTemplateMap = new HashMap<>();
        Field[] defaultTemplateFields = FieldUtils.getAllFields(NoticeConstants.TemplateText.class);
        setFieldNameMap(defaultTemplateFields, defaultTemplateMap);
        return defaultTemplateMap;
    }

    /**
     * 获取事件的默认模版标题
     *
     * @return Map<String, String> defaultTemplateTitleMap
     */
    public static Map<String, String> getDefaultTemplateTitleMap() {
        Map<String, String> defaultTemplateTitleMap = new HashMap<>();
        Field[] defaultTemplateTitleFields = FieldUtils.getAllFields(NoticeConstants.TemplateTitle.class);
        MessageTemplateUtils.setFieldNameMap(defaultTemplateTitleFields, defaultTemplateTitleMap);
        return defaultTemplateTitleMap;
    }

    /**
     * 获取接收人的特殊值
     * @return List<String> defaultRelatedUsers
     */
    public static List<String> getDefaultRelatedUser() {
        Field[] defaultRelatedUserFields = FieldUtils.getAllFields(NoticeConstants.RelatedUser.class);
        List<String> defaultRelatedUsers = new ArrayList<>();
        for (Field defaultRelatedUserField : defaultRelatedUserFields) {
            defaultRelatedUsers.add(defaultRelatedUserField.getName());
        }
        return defaultRelatedUsers;
    }

}
