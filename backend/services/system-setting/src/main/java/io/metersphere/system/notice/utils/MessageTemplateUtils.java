package io.metersphere.system.notice.utils;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.bug.domain.Bug;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.load.domain.LoadTest;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.ui.domain.UiScenario;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * @return FieldSourceMap
     */
    public static Map<String, String> getFieldSourceMap() {
        Map<String, String> fieldSourceMap = new HashMap<>();
        Field[] defaultRelatedUserFields = FieldUtils.getAllFields(NoticeConstants.FieldSource.class);
        MessageTemplateUtils.setFieldNameMap(defaultRelatedUserFields, fieldSourceMap);
        return fieldSourceMap;
    }

    public static Field[] getDomainTemplateFields(String taskType) {
        Field[] allFields;
        switch (taskType) {
            case NoticeConstants.TaskType.API_DEFINITION_TASK -> {
                allFields = FieldUtils.getAllFields(ApiDefinition.class);

            }
            case NoticeConstants.TaskType.API_SCENARIO_TASK -> {
                allFields = FieldUtils.getAllFields(ApiScenario.class);

            }
            case NoticeConstants.TaskType.TEST_PLAN_TASK -> {
                allFields = FieldUtils.getAllFields(TestPlan.class);

            }
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> {
                allFields = FieldUtils.getAllFields(CaseReview.class);
            }
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> {
                allFields = FieldUtils.getAllFields(FunctionalCase.class);
            }
            case NoticeConstants.TaskType.BUG_TASK -> {
                allFields = FieldUtils.getAllFields(Bug.class);
            }
            case NoticeConstants.TaskType.UI_SCENARIO_TASK -> {
                allFields = FieldUtils.getAllFields(UiScenario.class);
            }
            case NoticeConstants.TaskType.LOAD_TEST_TASK -> {
                allFields = FieldUtils.getAllFields(LoadTest.class);
            }
            case NoticeConstants.TaskType.SCHEDULE_TASK -> {
                allFields = FieldUtils.getAllFields(Schedule.class);
            }
            default -> allFields = new Field[0];
        }

        return allFields;
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
        StringSubstitutor sub = new StringSubstitutor(context);
        return sub.replace(template);
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

    public static String getTranslateTemplate(String taskType, String template) {
        if (StringUtils.equalsIgnoreCase(taskType, NoticeConstants.TaskType.JENKINS_TASK)) {
            if (StringUtils.isNotBlank(template) && template.contains("${name}")) {
                template = template.replace("${name}", "<"+Translator.get("message.jenkins_name")+">");
            }
            return template;
        } else {
            Field[] domainTemplateFields = getDomainTemplateFields(taskType);
            Map<String, Object> map = new HashMap<>();
            if (StringUtils.isNotBlank(template) && template.contains("${OPERATOR}")) {
                template = template.replace("${OPERATOR}", "<"+Translator.get("message.operator")+">");
            }
            if (StringUtils.isNotBlank(template) && template.contains("${total}")) {
                template = template.replace("${total}", "<n>");
            }
            for (Field allField : domainTemplateFields) {
                Schema annotation = allField.getAnnotation(Schema.class);
                if (annotation != null) {
                    String description = annotation.description();
                    if (StringUtils.equals(allField.getName(), "name") || StringUtils.equals(allField.getName(), "title")) {
                        description = "{{" + description + "}}";
                    }
                    map.put(allField.getName(), description);
                }
            }
            return getContent(template, map);
        }
    }
}
