package io.metersphere.system.notice.utils;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.BugMessageDTO;
import io.metersphere.system.dto.sdk.*;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.text.StringSubstitutor;

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
    public static Map<String, String> getDefaultTemplateSubjectMap() {
        Map<String, String> defaultTemplateSubjectMap = new HashMap<>();
        Field[] defaultTemplateSubjectFields = FieldUtils.getAllFields(NoticeConstants.TemplateSubject.class);
        MessageTemplateUtils.setFieldNameMap(defaultTemplateSubjectFields, defaultTemplateSubjectMap);
        return defaultTemplateSubjectMap;
    }

    /**
     * 获取接收人的特殊值
     *
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

    /**
     * 获取接收人的特殊值
     *
     * @return List<String> defaultRelatedUsers
     */
    public static Map<String, String> getDefaultRelatedUserMap() {
        Map<String, String> defaultRelatedUserMap = new HashMap<>();
        Field[] defaultRelatedUserFields = FieldUtils.getAllFields(NoticeConstants.RelatedUser.class);
        MessageTemplateUtils.setFieldNameMap(defaultRelatedUserFields, defaultRelatedUserMap);
        return defaultRelatedUserMap;
    }

    /**
     * 获取字段来源
     *
     * @return FieldSourceMap
     */
    public static Map<String, String> getFieldSourceMap() {
        Map<String, String> fieldSourceMap = new HashMap<>();
        Field[] defaultRelatedUserFields = FieldUtils.getAllFields(NoticeConstants.FieldSource.class);
        MessageTemplateUtils.setFieldNameMap(defaultRelatedUserFields, fieldSourceMap);
        return fieldSourceMap;
    }

    public static Field[] getDomainTemplateFields(String taskType) {
       return switch (taskType) {
            case NoticeConstants.TaskType.API_DEFINITION_TASK -> FieldUtils.getAllFields(ApiDefinitionCaseDTO.class);
            case NoticeConstants.TaskType.API_SCENARIO_TASK -> FieldUtils.getAllFields(ApiScenarioMessageDTO.class);
            case NoticeConstants.TaskType.API_REPORT_TASK -> FieldUtils.getAllFields(ApiReportMessageDTO.class);
            case NoticeConstants.TaskType.TEST_PLAN_TASK, NoticeConstants.TaskType.JENKINS_TASK -> FieldUtils.getAllFields(TestPlanMessageDTO.class);
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> FieldUtils.getAllFields(CaseReview.class);
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> FieldUtils.getAllFields(FunctionalCaseMessageDTO.class);
            case NoticeConstants.TaskType.BUG_TASK -> FieldUtils.getAllFields(BugMessageDTO.class);
            case NoticeConstants.TaskType.SCHEDULE_TASK -> FieldUtils.getAllFields(Schedule.class);
           default -> new Field[0];
        };
    }

    public static String getContent(String template, Map<String, Object> context) {
        // 处理 null
        context.forEach((k, v) -> {
            if (v == null) {
                context.put(k, StringUtils.EMPTY);
            }
        });
        // 处理时间格式的数据
        handleTime(context);
        // 处理人相关的数据
        handleUser(context);
        StringSubstitutor sub = new StringSubstitutor(context);
        String replace = sub.replace(template);
        return replace;
    }

    public static void handleTime(Map<String, Object> context) {
        context.forEach((k, v) -> {
            if (StringUtils.endsWithIgnoreCase(k, "Time")) {
                try {
                    String value = v.toString();
                    long time = Long.parseLong(value);
                    v = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
                    context.put(k, v);
                } catch (Exception ignore) {
                }
            }
        });
    }
    public static void handleUser(Map<String, Object> context) {
        UserMapper userMapper = CommonBeanFactory.getBean(UserMapper.class);
        context.forEach((k, v) -> {
            if (StringUtils.endsWithIgnoreCase(k, "User")) {
                try {
                    String value = v.toString();
                    assert userMapper != null;
                    User user = userMapper.selectByPrimaryKey(value);
                    context.put(k, user.getName());
                } catch (Exception ignore) {
                }
            }
        });
    }

    public static String getTranslateTemplate(String taskType, String template, Map<String, List<CustomField>> customFielddMap) {
        Field[] domainTemplateFields = getDomainTemplateFields(taskType);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(template) && template.contains("${OPERATOR}")) {
            template = template.replace("${OPERATOR}", "<" + Translator.get("message.operator") + ">");
        }
        if (StringUtils.isNotBlank(template) && template.contains("${total}")) {
            template = template.replace("${total}", "<n>");
        }
        setMap(taskType, domainTemplateFields, map);
        Map<String, String> defaultRelatedUserMap = getDefaultRelatedUserMap();
        defaultRelatedUserMap.remove("FOLLOW_PEOPLE");
        map.putAll(defaultRelatedUserMap);
        addCustomFiled(taskType, customFielddMap, map);
        return getContent(template, map);
    }

    private static void addCustomFiled(String taskType, Map<String, List<CustomField>> customFielddMap, Map<String, Object> map) {
        for (TemplateScene value : TemplateScene.values()) {
            if (taskType.contains(value.toString())) {
                List<CustomField> customFields = customFielddMap.get(value.toString());
                if (CollectionUtils.isNotEmpty(customFields)) {
                    Map<String, String> customFielddNameMap = new HashMap<>();
                    for (CustomField customField : customFields) {
                        customFielddNameMap.put(customField.getName(), StringUtils.isBlank(customField.getName()) ? "-" : "<" + customField.getName() + ">");
                    }
                    map.putAll(customFielddNameMap);
                }
            }
        }
    }

    private static void setMap(String taskType, Field[] domainTemplateFields, Map<String, Object> map) {
        switch (taskType) {
            case NoticeConstants.TaskType.API_DEFINITION_TASK, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK ->
                    putDescription(domainTemplateFields, map);
            case NoticeConstants.TaskType.API_SCENARIO_TASK -> putDescription(domainTemplateFields, map);
            case NoticeConstants.TaskType.API_REPORT_TASK -> putDescription(domainTemplateFields, map);
            case NoticeConstants.TaskType.TEST_PLAN_TASK, NoticeConstants.TaskType.JENKINS_TASK -> putDomainName(domainTemplateFields, map, "test_plan_");
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> putDomainName(domainTemplateFields, map, "case_review_");
            case NoticeConstants.TaskType.BUG_TASK -> putDomainName(domainTemplateFields, map, "bug_");
            case NoticeConstants.TaskType.SCHEDULE_TASK -> putDomainName(domainTemplateFields, map, "schedule_");
            default -> {}
        }
    }

    private static void putDomainName(Field[] domainTemplateFields, Map<String, Object> map, String tableName) {
        for (Field allField : domainTemplateFields) {
            Schema annotation = allField.getAnnotation(Schema.class);
            if (annotation != null) {
                String description;
                if (StringUtils.equals(allField.getName(), "name") || StringUtils.equals(allField.getName(), "title")) {
                    description = "{{" + Translator.get("message.domain." + tableName + allField.getName()) + "}}";
                } else {
                    description = "<" + Translator.get("message.domain." + tableName + allField.getName()) + ">";
                }
                map.put(allField.getName(), description);
            }
        }
    }

    private static void putDescription(Field[] domainTemplateFields, Map<String, Object> map) {
        for (Field allField : domainTemplateFields) {
            Schema annotation = allField.getAnnotation(Schema.class);
            if (annotation != null) {
                String description;
                if (StringUtils.equals(allField.getName(), "name") || StringUtils.equals(allField.getName(), "title")) {
                    description = "{{" + Translator.get("message.domain." + allField.getName()) + "}}";
                } else {
                    description = "<" + Translator.get("message.domain." + allField.getName()) + ">";
                }
                map.put(allField.getName(), description);
            }
        }
    }

    public static String getTranslateSubject(String taskType, String subject, Map<String, List<CustomField>> customFielddMap) {
        Field[] domainTemplateFields = getDomainTemplateFields(taskType);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(subject) && subject.contains("${OPERATOR}")) {
            subject = subject.replace("${OPERATOR}", Translator.get("message.operator"));
        }
        if (StringUtils.isNotBlank(subject) && subject.contains("${total}")) {
            subject = subject.replace("${total}", "n");
        }
        setMap(taskType, domainTemplateFields, map);
        Map<String, String> defaultRelatedUserMap = getDefaultRelatedUserMap();
        defaultRelatedUserMap.remove("FOLLOW_PEOPLE");
        map.putAll(defaultRelatedUserMap);
        addCustomFiled(taskType, customFielddMap, map);
        return getContent(subject, map);
    }


}
