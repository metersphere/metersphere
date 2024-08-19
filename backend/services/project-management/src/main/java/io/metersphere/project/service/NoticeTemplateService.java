package io.metersphere.project.service;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.project.dto.MessageTemplateFieldDTO;
import io.metersphere.project.dto.MessageTemplateResultDTO;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.BugMessageDTO;
import io.metersphere.system.dto.BugSyncNoticeDTO;
import io.metersphere.system.dto.request.DefaultBugCustomField;
import io.metersphere.system.dto.request.DefaultFunctionalCustomField;
import io.metersphere.system.dto.sdk.*;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class NoticeTemplateService {

    @Resource
    protected CustomFieldMapper customFieldMapper;

    public List<MessageTemplateFieldDTO> getDomainTemplateFields(String projectId, String taskType) {
        List<MessageTemplateFieldDTO> messageTemplateFieldDTOList = new ArrayList<>();
        switch (taskType) {
            case NoticeConstants.TaskType.API_DEFINITION_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(ApiDefinitionCaseDTO.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, null);
                addCustomFiled(messageTemplateFieldDTOList, projectId, TemplateScene.API.toString());
            }
            case NoticeConstants.TaskType.API_SCENARIO_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(ApiScenarioMessageDTO.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, null);
            }
            case NoticeConstants.TaskType.API_REPORT_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(ApiReportMessageDTO.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, null);
            }
            case NoticeConstants.TaskType.TEST_PLAN_TASK, NoticeConstants.TaskType.JENKINS_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(TestPlanMessageDTO.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, null);
            }
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(CaseReview.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, "case_review_");
            }
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(FunctionalCaseMessageDTO.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, null);
                addCustomFiled(messageTemplateFieldDTOList, projectId, TemplateScene.FUNCTIONAL.toString());
            }
            case NoticeConstants.TaskType.BUG_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(BugMessageDTO.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, null);
                addCustomFiled(messageTemplateFieldDTOList, projectId, TemplateScene.BUG.toString());
            }
            case NoticeConstants.TaskType.BUG_SYNC_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(BugSyncNoticeDTO.class);
                // TODO: 待修改, 同步仅需的字段{操作人, 触发方式}
                // 该方法提供了统一的内置通知模板字段; {操作人, 关注人, 触发方式}
                addOptionDto(messageTemplateFieldDTOList, allFields, null);
            }
            case NoticeConstants.TaskType.SCHEDULE_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(Schedule.class);
                addOptionDto(messageTemplateFieldDTOList, allFields, "schedule_");
            }
            default -> messageTemplateFieldDTOList = new ArrayList<>();
        }

        return messageTemplateFieldDTOList;
    }

    /**
     * 添加自定义字段
     *
     * @param messageTemplateFieldDTOS messageTemplateFieldDTOS
     * @param projectId                projectId
     * @param scene                    对应场景
     */
    private void addCustomFiled(List<MessageTemplateFieldDTO> messageTemplateFieldDTOS, String projectId, String scene) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andScopeIdEqualTo(projectId).andSceneEqualTo(scene);
        List<CustomField> customFields = customFieldMapper.selectByExample(example);
        for (CustomField customField : customFields) {
            MessageTemplateFieldDTO messageTemplateFieldDTO = new MessageTemplateFieldDTO();
            messageTemplateFieldDTO.setId(customField.getName());
            if (StringUtils.equalsAnyIgnoreCase(customField.getName(),
                    DefaultBugCustomField.DEGREE.getName(), DefaultFunctionalCustomField.PRIORITY.getName())) {
                // 缺陷严重程度, 用例等级 作为系统内置的自定义字段需要国际化后在模板展示
                messageTemplateFieldDTO.setName(Translator.get("custom_field." + customField.getName()));
            } else {
                messageTemplateFieldDTO.setName(customField.getName());
            }
            messageTemplateFieldDTO.setFieldSource(NoticeConstants.FieldSource.CUSTOM_FIELD);
            messageTemplateFieldDTOS.add(messageTemplateFieldDTO);
        }
    }

    /**
     * 添加数据库字段
     *
     * @param messageTemplateFieldDTOS messageTemplateFieldDTOS
     * @param allFields                allFields
     */
    private static void addOptionDto(List<MessageTemplateFieldDTO> messageTemplateFieldDTOS, Field[] allFields, String tableName) {
        Field[] sensitiveFields = FieldUtils.getAllFields(NoticeConstants.SensitiveField.class);
        ArrayList<Field> sensitiveFieldList = new ArrayList<>(sensitiveFields.length);
        Collections.addAll(sensitiveFieldList, sensitiveFields);
        List<String> nameList = sensitiveFieldList.stream().map(Field::getName).toList();
        for (Field allField : allFields) {
            MessageTemplateFieldDTO messageTemplateFieldDTO = new MessageTemplateFieldDTO();
            if (nameList.contains(allField.getName())) {
                continue;
            }

            Schema annotation = allField.getAnnotation(Schema.class);
            if (annotation != null) {
                messageTemplateFieldDTO.setId(allField.getName());
                String description;
                if (StringUtils.isBlank(tableName)) {
                    description = Translator.get(annotation.description());
                } else {
                    description = Translator.get("message.domain." + tableName + allField.getName());
                }
                messageTemplateFieldDTO.setName(description);
                messageTemplateFieldDTO.setFieldSource(NoticeConstants.FieldSource.CASE_FIELD);
                messageTemplateFieldDTOS.add(messageTemplateFieldDTO);
            }
        }
        MessageTemplateFieldDTO messageTemplateFieldOperator = new MessageTemplateFieldDTO();
        messageTemplateFieldOperator.setId(NoticeConstants.RelatedUser.OPERATOR);
        messageTemplateFieldOperator.setFieldSource(NoticeConstants.FieldSource.CASE_FIELD);
        messageTemplateFieldOperator.setName(Translator.get("message.operator"));
        messageTemplateFieldDTOS.add(messageTemplateFieldOperator);
    }

    public MessageTemplateResultDTO getTemplateFields(String projectId, String taskType) {
        MessageTemplateResultDTO messageTemplateResultDTO = new MessageTemplateResultDTO();
        List<MessageTemplateFieldDTO> domainTemplateFields = getDomainTemplateFields(projectId, taskType);
        messageTemplateResultDTO.setFieldList(domainTemplateFields);
        Map<String, String> fieldSourceMap = MessageTemplateUtils.getFieldSourceMap();
        if (!StringUtils.equalsIgnoreCase(taskType, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK) && !StringUtils.equalsIgnoreCase(taskType, NoticeConstants.TaskType.BUG_TASK)) {
            fieldSourceMap.remove(NoticeConstants.FieldSource.CUSTOM_FIELD);
        }
        if (!StringUtils.equalsIgnoreCase(taskType, NoticeConstants.TaskType.API_REPORT_TASK) && !StringUtils.equalsIgnoreCase(taskType, NoticeConstants.TaskType.TEST_PLAN_REPORT_TASK)) {
            fieldSourceMap.remove(NoticeConstants.FieldSource.REPORT_FIELD);
        }
        List<OptionDTO> optionDTOList = new ArrayList<>();
        fieldSourceMap.forEach((k, v) -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(k);
            optionDTO.setName(v);
            optionDTOList.add(optionDTO);
        });
        messageTemplateResultDTO.setFieldSourceList(optionDTOList);
        return messageTemplateResultDTO;
    }

}
