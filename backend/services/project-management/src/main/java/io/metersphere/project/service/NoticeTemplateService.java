package io.metersphere.project.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.bug.domain.Bug;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.load.domain.LoadTest;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.ui.domain.UiScenario;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class NoticeTemplateService {

    public List<OptionDTO> getTemplateFields(String type) {
        List<OptionDTO> optionDTOList = new ArrayList<>();
        switch (type) {
            case NoticeConstants.TaskType.API_DEFINITION_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(ApiDefinition.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义模版字段
            }
            case NoticeConstants.TaskType.API_SCENARIO_TASK, NoticeConstants.TaskType.API_SCHEDULE_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(ApiScenario.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义
            }
            case NoticeConstants.TaskType.TEST_PLAN_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(TestPlan.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义
            }
            case NoticeConstants.TaskType.CASE_REVIEW_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(CaseReview.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义
            }
            case NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(FunctionalCase.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义
            }
            case NoticeConstants.TaskType.BUG_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(Bug.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义
            }
            case NoticeConstants.TaskType.UI_SCENARIO_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(UiScenario.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义
            }
            case NoticeConstants.TaskType.LOAD_TEST_TASK -> {
                Field[] allFields = FieldUtils.getAllFields(LoadTest.class);
                addOptionDto(optionDTOList, allFields);
                //TODO：获取报告和自定义
            }
        }
        return optionDTOList;
    }

    private static void addOptionDto(List<OptionDTO> optionDTOList, Field[] allFields) {
        for (Field allField : allFields) {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(allField.getName());
            Schema annotation = allField.getAnnotation(Schema.class);
            if (annotation != null) {
                String description = annotation.description();
                optionDTO.setName(description);
            }
            optionDTOList.add(optionDTO);
        }
    }
}
