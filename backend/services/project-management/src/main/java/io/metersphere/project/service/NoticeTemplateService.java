package io.metersphere.project.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.bug.domain.Bug;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.load.domain.LoadTest;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.ui.domain.UiScenario;
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

@Service
@Transactional(rollbackFor = Exception.class)
public class NoticeTemplateService {

    @Resource
    protected CustomFieldMapper customFieldMapper;

    public List<OptionDTO> getTemplateFields(String projectId, String taskType) {
        List<OptionDTO> optionDTOList = new ArrayList<>();
        switch (taskType) {
            case NoticeConstants.TaskType.API_DEFINITION_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(ApiDefinition.class);
                addOptionDto(optionDTOList, allFields);
                addCustomFiled(optionDTOList, projectId, TemplateScene.API.toString());
                //TODO：获取报告
            }
            case NoticeConstants.TaskType.API_SCENARIO_TASK, NoticeConstants.TaskType.API_SCHEDULE_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(ApiScenario.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告
            }
            case NoticeConstants.TaskType.TEST_PLAN_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(TestPlan.class);
                addOptionDto(optionDTOList, allFields);
                addCustomFiled(optionDTOList, projectId, TemplateScene.TEST_PLAN.toString());
                //TODO：获取报告
            }
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(CaseReview.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告
            }
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(FunctionalCase.class);
                addOptionDto(optionDTOList, allFields);
                addCustomFiled(optionDTOList, projectId, TemplateScene.FUNCTIONAL.toString());
                //TODO：获取报告
            }
            case NoticeConstants.TaskType.BUG_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(Bug.class);
                addOptionDto(optionDTOList, allFields);
                addCustomFiled(optionDTOList, projectId, TemplateScene.BUG.toString());
                //TODO：获取报告
            }
            case NoticeConstants.TaskType.UI_SCENARIO_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(UiScenario.class);
                addOptionDto(optionDTOList, allFields);
                addCustomFiled(optionDTOList, projectId, TemplateScene.UI.toString());
                //TODO：获取报告
            }
            case NoticeConstants.TaskType.LOAD_TEST_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(LoadTest.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告
            }
            default -> optionDTOList = new ArrayList<>();
        }
        return optionDTOList;
    }

    /**
     * 添加自定义字段
     * @param optionDTOList optionDTOList
     * @param projectId projectId
     * @param scene 对应场景
     */
    private void addCustomFiled(List<OptionDTO> optionDTOList, String projectId, String scene) {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andScopeIdEqualTo(projectId).andSceneEqualTo(scene);
        List<CustomField> customFields = customFieldMapper.selectByExample(example);
        for (CustomField customField : customFields) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(customField.getName());
            optionDTO.setName(StringUtils.isBlank(customField.getRemark()) ? "-" : customField.getRemark());
            optionDTOList.add(optionDTO);
        }
    }

    /**
     * 添加数据库字段
     * @param optionDTOList optionDTOList
     * @param allFields allFields
     */
    private static void addOptionDto(List<OptionDTO> optionDTOList, Field[] allFields) {
        Field[] sensitiveFields = FieldUtils.getAllFields(NoticeConstants.SensitiveField.class);
        ArrayList<Field> sensitiveFieldList = new ArrayList<>(sensitiveFields.length);
        Collections.addAll(sensitiveFieldList, sensitiveFields);
        List<String> nameList = sensitiveFieldList.stream().map(Field::getName).toList();
        for (Field allField : allFields) {
            OptionDTO optionDTO = new OptionDTO();
            if (nameList.contains(allField.getName())) {
                continue;
            }
            optionDTO.setId(allField.getName());
            Schema annotation = allField.getAnnotation(Schema.class);
            if (annotation != null) {
                String description = annotation.description();
                optionDTO.setName(description);
                optionDTOList.add(optionDTO);
            }
        }
    }

}
